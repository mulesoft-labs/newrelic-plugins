package org.mule.newrelic.plugins.artemis.responses;

import java.util.ArrayList;
import java.util.List;

public class JolokiaResult {

    private final List<MetricResult> metricResults = new ArrayList<>();

    public List<MetricResult> getMetricResults() {
        return metricResults;
    }

    public void addMetric(String metricName, String units, Number value) {
        metricResults.add(new MetricResult(metricName, units, value));
    }

    public static final class MetricResult {
        private final String metricName;
        private final String units;
        private final Number value;

        public MetricResult(String metricName, String units, Number value) {
            this.metricName = metricName;
            this.units = units;
            this.value = value;
        }

        public String getMetricName() {
            return metricName;
        }

        public String getUnits() {
            return units;
        }

        public Number getValue() {
            return value;
        }
    }
}
