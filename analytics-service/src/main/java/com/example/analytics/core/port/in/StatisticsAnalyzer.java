package com.example.analytics.core.port.in;

import com.example.analytics.core.domain.ColumnStatistics;
import com.example.analytics.core.domain.DatasetData;

import java.util.List;

public interface StatisticsAnalyzer {
    List<ColumnStatistics> analyze(DatasetData datasetData);
}
