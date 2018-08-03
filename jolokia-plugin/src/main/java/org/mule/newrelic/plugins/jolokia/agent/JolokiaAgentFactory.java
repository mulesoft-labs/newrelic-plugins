package org.mule.newrelic.plugins.jolokia.agent;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;
import org.mule.newrelic.plugins.jolokia.probes.JolokiaProbe;
import org.mule.newrelic.plugins.jolokia.reporters.JolokiaReporter;

import java.util.Map;

public class JolokiaAgentFactory extends AgentFactory {

    private static final Logger logger = Logger.getLogger(JolokiaAgentFactory.class);

    public JolokiaAgentFactory() {
    }

    @Override
    public Agent createConfiguredAgent(Map<String, Object> pluginProperties) throws ConfigurationException {
        AgentProperties properties = AgentProperties.parse(pluginProperties);
        logger.info("Creating agent using: ", properties);

        String pluginName = System.getProperty("plugin.name") != null ? System.getProperty("plugin.name") : JolokiaAgent.GUID;
        String pluginVersion = System.getProperty("plugin.version") != null ? System.getProperty("plugin.version") : JolokiaAgent.VERSION;

        JolokiaAgent agent = new JolokiaAgent(properties.getName(), pluginName, pluginVersion);
        registerProbes(properties, agent);
        return agent;
    }

    private void registerProbes(AgentProperties properties, JolokiaAgent agent) {
        NewRelicAgent newRelicAgentMetricAware = new NewRelicAgentDecorator(agent);
        agent.add(new JolokiaProbe(properties), new JolokiaReporter(newRelicAgentMetricAware));
    }
}