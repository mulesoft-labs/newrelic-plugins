package org.mule.newrelic.plugins.jolokia.reporters;

import com.google.common.base.Stopwatch;
import com.newrelic.metrics.publish.util.Logger;
import org.mule.newrelic.plugins.jolokia.agent.NewRelicAgent;
import org.mule.newrelic.plugins.jolokia.responses.ApplicationResponse;
import org.mule.newrelic.plugins.jolokia.responses.JolokiaResult;

import java.util.concurrent.TimeUnit;

public class JolokiaReporter implements NewRelicReporter<JolokiaResult> {

    private static final Logger logger = Logger.getLogger(JolokiaReporter.class);

    private final NewRelicAgent agent;

    public JolokiaReporter(NewRelicAgent agent) {
        this.agent = agent;
    }

    @Override
    public void report(ApplicationResponse<JolokiaResult> response) {
        Stopwatch sw = Stopwatch.createStarted();
        if (response.isError()) {
            logger.error(response.getError(), " response is an error");
            return;
        }

        JolokiaResult result = response.getResult();

        logger.info("Reporting ", result.getMetricResults().size(), " metrics");
        for (JolokiaResult.MetricResult metricResult : result.getMetricResults()) {
            logger.debug("MetricName: ", metricResult.getMetricName(), " - Units: ", metricResult.getUnits(), " - Value: ", metricResult.getValue());
            agent.reportMetric(metricResult.getMetricName(), metricResult.getUnits(), metricResult.getValue());
        }
        logger.info("JolokiaReporter finish after: ", sw.elapsed(TimeUnit.MILLISECONDS));
    }
}
