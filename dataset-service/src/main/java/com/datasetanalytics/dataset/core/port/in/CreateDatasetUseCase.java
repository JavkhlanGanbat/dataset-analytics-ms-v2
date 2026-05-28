package com.datasetanalytics.dataset.core.port.in;

import com.datasetanalytics.dataset.core.domain.Dataset;

public interface CreateDatasetUseCase {
    Dataset createEmptyDataset(String name);
    Dataset createDatasetFromCsv(String name, String csvContent);
}
