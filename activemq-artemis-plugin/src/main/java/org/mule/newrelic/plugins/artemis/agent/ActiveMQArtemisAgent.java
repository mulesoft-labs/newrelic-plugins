package org.mule.newrelic.plugins.artemis.agent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;
import org.mule.newrelic.plugins.artemis.probes.ApplicationProbe;
import org.mule.newrelic.plugins.artemis.reporters.NewRelicReporter;
import org.mule.newrelic.plugins.artemis.responses.ApplicationResponse;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class ActiveMQArtemisAgent extends Agent implements NewRelicAgent {

    private static final Logger logger = Logger.getLogger(ActiveMQArtemisAgent.class);

    public static final String GUID = "org.mule.newrelic.plugins.artemis";
    public static final String VERSION = "1.0.0";

    private final String name;
    private final List<Poller> pollers;

    public ActiveMQArtemisAgent(String name, String pluginName, String pluginVersion) {
        super(pluginName, pluginVersion);

        logger.info("Agent/Instance Name: ", name);
        logger.info("GUID: ", pluginName);
        logger.info("Version: ", pluginVersion);

        checkArgument(!Strings.isNullOrEmpty(name));

        this.name = name;
        this.pollers = new ArrayList<Poller>();
    }

    public void pollCycle() {
        for (Poller poller : pollers) {
            try {
                poller.run();
            } catch (Exception e) {
                logger.error(e, "Problem polling: ", poller);
            }
        }
    }

    public String getAgentName() {
        return name;
    }

    public <T> void add(ApplicationProbe<T> probe, NewRelicReporter<T> reporter) {
        pollers.add(new Poller<T>(probe, reporter));
    }

    private static class Poller<T> {
        private ApplicationProbe<T> probe;
        private NewRelicReporter<T> reporter;

        public Poller(ApplicationProbe<T> probe, NewRelicReporter<T> reporter) {
            this.probe = probe;
            this.reporter = reporter;
        }

        public void run() {
            ApplicationResponse<T> response = probe.probe();
            reporter.report(response);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("probe", probe)
                    .add("reporter", reporter)
                    .toString();
        }
    }
}
