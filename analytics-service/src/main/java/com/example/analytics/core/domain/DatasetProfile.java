package com.example.analytics.core.domain;

import java.util.List;

public record DatasetProfile(
        Long datasetId,
        String datasetName,
        long totalRowCount,
        int totalColumnCount,
        List<ColumnStatistics> columns,
        DataQualityScore dataQuality,
        List<String> insights
) {}
