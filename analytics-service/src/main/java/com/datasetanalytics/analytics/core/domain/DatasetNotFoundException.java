package com.datasetanalytics.analytics.core.domain;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(Long datasetId) {
        super("Dataset not found: " + datasetId);
    }
}
