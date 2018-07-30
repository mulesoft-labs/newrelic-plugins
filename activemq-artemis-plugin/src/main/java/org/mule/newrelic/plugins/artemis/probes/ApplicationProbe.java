package org.mule.newrelic.plugins.artemis.probes;

import org.mule.newrelic.plugins.artemis.responses.ApplicationResponse;

public interface ApplicationProbe<T> {
    ApplicationResponse<T> probe();
}
