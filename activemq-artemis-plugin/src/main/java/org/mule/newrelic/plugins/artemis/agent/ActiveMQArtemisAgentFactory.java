package org.mule.newrelic.plugins.artemis.agent;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;
import org.mule.newrelic.plugins.artemis.probes.JolokiaProbe;
import org.mule.newrelic.plugins.artemis.reporters.JolokiaReporter;

import java.util.Map;

public class ActiveMQArtemisAgentFactory extends AgentFactory {

    private static final Logger logger = Logger.getLogger(ActiveMQArtemisAgentFactory.class);

    public ActiveMQArtemisAgentFactory() {
    }

    @Override
    public Agent createConfiguredAgent(Map<String, Object> pluginProperties) throws ConfigurationException {
        AgentProperties properties = AgentProperties.parse(pluginProperties);
        logger.info("Creating agent using: ", properties);

        ActiveMQArtemisAgent agent = new ActiveMQArtemisAgent(properties.getName());
        registerProbes(properties, agent);
        return agent;
    }

    private void registerProbes(AgentProperties properties, ActiveMQArtemisAgent agent) {
        NewRelicAgent newRelicAgentMetricAware = new NewRelicAgentMetricAware(agent);
        agent.add(new JolokiaProbe(properties), new JolokiaReporter(newRelicAgentMetricAware));
    }
}