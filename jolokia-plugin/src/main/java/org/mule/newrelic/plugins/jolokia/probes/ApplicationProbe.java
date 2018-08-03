package org.mule.newrelic.plugins.jolokia.probes;

import org.mule.newrelic.plugins.jolokia.responses.ApplicationResponse;

public interface ApplicationProbe<T> {
    ApplicationResponse<T> probe();
}
