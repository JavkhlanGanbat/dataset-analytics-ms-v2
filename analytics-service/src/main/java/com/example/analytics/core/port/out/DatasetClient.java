package com.example.analytics.core.port.out;

import com.example.analytics.core.domain.DatasetData;

public interface DatasetClient {
    DatasetData fetchDatasetData(Long datasetId);
}
