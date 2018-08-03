package org.mule.newrelic.plugins.jolokia.responses;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApplicationResponse<T> {
    private T result;
    private Throwable error;

    private ApplicationResponse(T result, Throwable error) {
        this.result = result;
        this.error = error;
    }

    public static <T> ApplicationResponse<T> of(T result) {
        return new ApplicationResponse<T>(checkNotNull(result), null);
    }

    public static <T> ApplicationResponse<T> of(Throwable t) {
        return new ApplicationResponse<T>(null, checkNotNull(t));
    }

    public T getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isError() {
        return error != null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("result", result)
                .add("error", error)
                .toString();
    }
}