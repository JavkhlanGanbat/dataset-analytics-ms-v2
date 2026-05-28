package com.datasetanalytics.analytics.core.domain;

public class DatasetServiceUnavailableException extends RuntimeException {
    public DatasetServiceUnavailableException(String message) {
        super(message);
    }

    public DatasetServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
