package org.mule.newrelic.plugins.artemis.reporters;

import org.mule.newrelic.plugins.artemis.responses.ApplicationResponse;

public interface NewRelicReporter<T> {
    void report(ApplicationResponse<T> data);
}