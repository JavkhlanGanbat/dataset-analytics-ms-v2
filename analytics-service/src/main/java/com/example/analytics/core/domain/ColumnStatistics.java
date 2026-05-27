package com.example.analytics.core.domain;

import java.util.Map;

public record ColumnStatistics(
        String columnName,
        ColumnType inferredType,
        long totalValues,
        long missingValues,
        double missingPercentage,
        long distinctCount,
        long numericValueCount,
        Double min,
        Double max,
        Double mean,
        Double median,
        Double standardDeviation,
        long outlierCount,
        Map<String, Long> mostCommonValues,
        Double averageTextLength
) {}
