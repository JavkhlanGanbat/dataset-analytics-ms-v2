package com.example.dataset.core.domain;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(Long datasetId) {
        super("Dataset not found: " + datasetId);
    }
}
