package com.datasetanalytics.analytics.core.domain;

public class DatasetServiceTimeoutException extends RuntimeException {
    public DatasetServiceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
