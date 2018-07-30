package org.mule.newrelic.plugins.artemis;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import org.mule.newrelic.plugins.artemis.agent.ActiveMQArtemisAgentFactory;

public class Main {
    public static void main(String[] args) {
        try {
            Runner runner = new Runner();
            ActiveMQArtemisAgentFactory agentFactory = new ActiveMQArtemisAgentFactory();
            runner.add(agentFactory);
            runner.setupAndRun();
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(-1);
        }
    }
}