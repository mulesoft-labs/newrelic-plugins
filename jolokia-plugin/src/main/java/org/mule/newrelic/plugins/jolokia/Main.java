package org.mule.newrelic.plugins.jolokia;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import org.mule.newrelic.plugins.jolokia.agent.JolokiaAgentFactory;

public class Main {
    public static void main(String[] args) {
        try {
            Runner runner = new Runner();
            JolokiaAgentFactory agentFactory = new JolokiaAgentFactory();
            runner.add(agentFactory);
            runner.setupAndRun();
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(-1);
        }
    }
}