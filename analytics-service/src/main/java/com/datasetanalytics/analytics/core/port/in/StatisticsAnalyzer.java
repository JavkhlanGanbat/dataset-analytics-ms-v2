package com.datasetanalytics.analytics.core.port.in;

import com.datasetanalytics.analytics.core.domain.ColumnStatistics;
import com.datasetanalytics.analytics.core.domain.DatasetData;

import java.util.List;

public interface StatisticsAnalyzer {
    List<ColumnStatistics> analyze(DatasetData datasetData);
}
