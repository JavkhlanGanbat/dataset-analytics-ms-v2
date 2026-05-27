package com.example.analytics.core.domain;

public class DatasetServiceUnavailableException extends RuntimeException {
    public DatasetServiceUnavailableException(String message) {
        super(message);
    }

    public DatasetServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
