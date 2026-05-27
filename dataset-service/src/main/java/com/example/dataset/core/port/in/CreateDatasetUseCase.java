package com.example.dataset.core.port.in;

import com.example.dataset.core.domain.Dataset;

public interface CreateDatasetUseCase {
    Dataset createEmptyDataset(String name);
    Dataset createDatasetFromCsv(String name, String csvContent);
}
