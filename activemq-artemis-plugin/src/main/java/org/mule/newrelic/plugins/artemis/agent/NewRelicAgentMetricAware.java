package org.mule.newrelic.plugins.artemis.agent;

import com.newrelic.metrics.publish.util.Logger;

/**
 * Decorates an agent to provide additional reporting facilities
 */
public class NewRelicAgentMetricAware implements NewRelicAgent {
    private static final Logger logger = Logger.getLogger(NewRelicAgentMetricAware.class);

    private NewRelicAgent agent;

    public NewRelicAgentMetricAware(NewRelicAgent agent) {
        this.agent = agent;
    }

    @Override
    public void reportMetric(String metricName, String units, Number value) {
        agent.reportMetric(metricName, units, value);
    }

    @Override
    public void reportMetric(String metricName, String units, int count, Number value, Number minValue, Number maxValue, Number sumOfSquares) {
        agent.reportMetric(metricName, units, count, value, minValue, maxValue, sumOfSquares);
    }
}