package org.mule.newrelic.plugins.artemis.probes;

import com.arakelian.jq.ImmutableJqRequest;
import com.arakelian.jq.JqRequest;
import com.arakelian.jq.JqResponse;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.newrelic.metrics.publish.util.Logger;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pRequest;
import org.jolokia.client.request.J4pResponse;
import org.mule.newrelic.plugins.artemis.agent.AgentProperties;
import org.mule.newrelic.plugins.artemis.responses.ApplicationResponse;
import org.mule.newrelic.plugins.artemis.responses.JolokiaResult;
import org.mule.newrelic.plugins.artemis.util.JQHolder;

import javax.management.MalformedObjectNameException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JolokiaProbe implements ApplicationProbe<JolokiaResult> {

    private static final Logger logger = Logger.getLogger(JolokiaProbe.class);
    private static final Pattern REQUEST_IDENTIFIER_PATTER = Pattern.compile("(?<objectName>.+)\\[(?<attributeName>[a-zA-Z]+)?]\\[(?<groupNumber>\\d+)]");

    private final J4pClient jolokiaClient;

    private final Map<Long, List<JMXRequest>> requestsByGroup = new TreeMap<>();

    public JolokiaProbe(AgentProperties properties) {
        try {
            this.jolokiaClient = createJolokiaClient(properties);

            List<AgentProperties.JMXMetric> jmxMetrics = properties.getMetrics();

            Map<String, J4pRequest> jolokiaRequests = new HashMap<>();

            for (AgentProperties.JMXMetric jmxMetric : jmxMetrics) {
                String requestIdentifier = jmxMetric.getObjectName() + "[" + jmxMetric.getAttributeName() + "]" + "[" + jmxMetric.getGroupNumber() + "]";

                try {
                    J4pRequest jolokiaRequest = jmxMetric.hasAttribute() ? new J4pReadRequest(jmxMetric.getObjectName(), jmxMetric.getAttributeName()) : new J4pReadRequest(jmxMetric.getObjectName());

                    jolokiaRequests.put(requestIdentifier, jolokiaRequest);
                } catch (MalformedObjectNameException e) {
                    logger.error(e, "Error creating metric, please validate object name and attribute name [", jmxMetric.getObjectName(), ", ", jmxMetric.getAttributeName(), "]");
                }
            }

            for (Map.Entry<String, J4pRequest> entry : jolokiaRequests.entrySet()) {
                String requestIdentifier = entry.getKey();
                Matcher matcher = REQUEST_IDENTIFIER_PATTER.matcher(requestIdentifier);
                if (matcher.matches()) {
                    String objectName = matcher.group("objectName");
                    String attributeName = Strings.nullToEmpty(matcher.group("attributeName"));
                    Long groupNumber = Long.valueOf(matcher.group("groupNumber"));

                    List<MetricData> metricData = buildMetricExpressions(jmxMetrics, objectName, attributeName, groupNumber);

                    requestsByGroup.compute(groupNumber, (k, v) -> {
                        if (v == null) {
                            v = new ArrayList<>();
                        }

                        v.add(new JMXRequest(entry.getValue(), metricData));

                        return v;
                    });
                } else {
                    logger.error("Request Identifier does not match, please review it");
                }
            }
        } catch (Exception e) {
            logger.error(e, "Error creating JolokiaProbe");
            throw e;
        }
    }

    @Override
    public ApplicationResponse<JolokiaResult> probe() {
        Stopwatch sw = Stopwatch.createStarted();
        JolokiaResult result = new JolokiaResult();
        for (Map.Entry<Long, List<JMXRequest>> entry : requestsByGroup.entrySet()) {
            List<JMXRequest> jmxRequests = entry.getValue();

            List<J4pRequest> jolokiaRequests = new ArrayList<>(jmxRequests.size());
            for (JMXRequest jmxRequest : jmxRequests) {
                jolokiaRequests.add(jmxRequest.getJolokiaRequest());
            }

            try {
                List<J4pResponse<J4pRequest>> responses = jolokiaClient.execute(jolokiaRequests);

                for (int index = 0; index < responses.size(); index++) {
                    J4pResponse<J4pRequest> response = responses.get(index);
                    JMXRequest jmxRequest = jmxRequests.get(index);
                    String jsonString = response.asJSONObject().toJSONString();

                    for (MetricData metricData : jmxRequest.getMetricData()) {
                        JqRequest jqRequest = ImmutableJqRequest.builder() //
                                .lib(JQHolder.LIBRARY) //
                                .input(jsonString) //
                                .filter(metricData.getJqExpression()) //
                                .build();

                        JqResponse jqResponse = jqRequest.execute();
                        if (!jqResponse.hasErrors()) {
                            String output = jqResponse.getOutput();
                            Number value = NumberFormat.getInstance().parse(output);

                            result.addMetric(metricData.getMetricName(), metricData.getUnits(), value);
                        } else {
                            logger.error("JQ Errors");
                            logger.error(jqResponse.getErrors());
                        }
                    }
                }
            } catch (J4pException e) {
                logger.error("Error executing bulk request after: ", sw.elapsed(TimeUnit.MILLISECONDS));
                return ApplicationResponse.of(e);
            } catch (ParseException e) {
                logger.error("Error parsing string to number after: ", sw.elapsed(TimeUnit.MILLISECONDS));
                return ApplicationResponse.of(e);
            } catch (Exception e) {
                logger.error("Error while executing probe after: ", sw.elapsed(TimeUnit.MILLISECONDS));
                return ApplicationResponse.of(e);
            }
        }
        logger.info("JolokiaProbe finish after: ", sw.elapsed(TimeUnit.MILLISECONDS));
        return ApplicationResponse.of(result);
    }

    private J4pClient createJolokiaClient(AgentProperties properties) {
        return new J4pClient(properties.getProtocol() + "://" + properties.getHost() + ":" + properties.getPort() + "/" + properties.getPath() + "/");
    }

    private List<MetricData> buildMetricExpressions(List<AgentProperties.JMXMetric> jmxMetrics, String objectName, String attributeName, Long groupNumber) {
        List<MetricData> metricData = new ArrayList<>();

        for (AgentProperties.JMXMetric jmxMetric : jmxMetrics) {
            if (jmxMetric.getObjectName().equals(objectName) && jmxMetric.getAttributeName().equals(attributeName) && jmxMetric.getGroupNumber().equals(groupNumber)) {
                metricData.add(new MetricData(jmxMetric.getMetricName(), jmxMetric.getUnits(), jmxMetric.getJqExpression()));
            }
        }
        return metricData;
    }

    private static final class JMXRequest {
        private final J4pRequest jolokiaRequest;
        private final List<MetricData> metricData;

        public JMXRequest(J4pRequest jolokiaRequest, List<MetricData> metricData) {
            this.jolokiaRequest = jolokiaRequest;
            this.metricData = metricData;
        }

        public J4pRequest getJolokiaRequest() {
            return jolokiaRequest;
        }

        public List<MetricData> getMetricData() {
            return metricData;
        }
    }

    private static final class MetricData {
        private final String metricName;
        private final String units;
        private final String jqExpression;

        public MetricData(String metricName, String units, String jqExpression) {
            this.metricName = metricName;
            this.units = units;
            this.jqExpression = jqExpression;
        }

        public String getMetricName() {
            return metricName;
        }

        public String getUnits() {
            return units;
        }

        public String getJqExpression() {
            return jqExpression;
        }
    }
}
