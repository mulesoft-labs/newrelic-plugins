package org.mule.newrelic.plugins.jolokia.reporters;

import org.mule.newrelic.plugins.jolokia.responses.ApplicationResponse;

public interface NewRelicReporter<T> {
    void report(ApplicationResponse<T> data);
}