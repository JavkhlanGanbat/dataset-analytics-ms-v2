package com.datasetanalytics.analytics.core.port.out;

import com.datasetanalytics.analytics.core.domain.DatasetData;

public interface DatasetClient {
    DatasetData fetchDatasetData(Long datasetId);
}
