package org.mule.newrelic.plugins.jolokia.agent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgentProperties {
    private Map<String, Object> properties;

    private AgentProperties() {
    }

    public static AgentProperties parse(Map<String, Object> properties) {
        AgentProperties agentProperties = new AgentProperties();
        agentProperties.properties = properties;
        return agentProperties;
    }

    public String getName() {
        String name = (String) properties.get("name");
        return Strings.isNullOrEmpty(name) ? getHost() : name;
    }

    public String getProtocol() {
        if (properties.containsKey("protocol")) {
            return (String) properties.get("protocol");
        } else {
            return "http";
        }
    }

    public String getHost() {
        return (String) properties.get("host");
    }

    public int getPort() {
        if (properties.containsKey("port")) {
            return ((Number) properties.get("port")).intValue();
        } else {
            return 8161;
        }
    }

    public String getPath() {
        if (properties.containsKey("path")) {
            return Strings.nullToEmpty((String) properties.get("path"));
        } else {
            return "jolokia";
        }
    }

    public List<JMXMetric> getMetrics() {
        List<JMXMetric> jmxMetrics = new ArrayList<>();
        if (properties.containsKey("metrics")) {
            JSONArray metrics = (JSONArray) properties.get("metrics");
            for (Object jsonObject : metrics) {
                JSONObject metric = (JSONObject) jsonObject;

                Long groupNumber = (Long) metric.get("group_number");
                String metricName = (String) metric.get("metric_name");
                String units = (String) metric.get("units");
                String objectName = (String) metric.get("object_name");
                String attributeName = (String) metric.get("attribute_name");
                String jqExpression = (String) metric.get("jq_expression");

                JMXMetric jmxMetric = new JMXMetric(groupNumber != null ? groupNumber : 1L, metricName, units, objectName, Strings.nullToEmpty(attributeName), jqExpression);

                jmxMetrics.add(jmxMetric);
            }
        }
        return jmxMetrics;
    }

    public String getUser() {
        if (properties.containsKey("user")) {
            return Strings.nullToEmpty((String) properties.get("user"));
        } else {
            return null;
        }
    }

    public String getPassword() {
        if (properties.containsKey("password")) {
            return Strings.nullToEmpty((String) properties.get("password"));
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(properties)
                .toString();
    }

    public static class JMXMetric {
        private Long groupNumber;
        private String metricName;
        private String units;
        private String objectName;
        private String attributeName;
        private String jqExpression;

        public JMXMetric(Long groupNumber, String metricName, String units, String objectName, String attributeName, String jqExpression) {
            this.groupNumber = groupNumber;
            this.metricName = metricName;
            this.units = units;
            this.objectName = objectName;
            this.attributeName = attributeName;
            this.jqExpression = jqExpression;
        }

        public Long getGroupNumber() {
            return groupNumber;
        }

        public String getMetricName() {
            return metricName;
        }

        public String getUnits() {
            return units;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public String getJqExpression() {
            return jqExpression;
        }

        public boolean hasAttribute() {
            return !Strings.isNullOrEmpty(getAttributeName());
        }
    }
}