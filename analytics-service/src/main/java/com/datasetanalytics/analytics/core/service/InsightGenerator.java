package com.datasetanalytics.analytics.core.service;

import com.datasetanalytics.analytics.core.domain.ColumnStatistics;
import com.datasetanalytics.analytics.core.domain.ColumnType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InsightGenerator {
    public List<String> generate(List<ColumnStatistics> columns) {
        List<String> insights = new ArrayList<>();

        for (ColumnStatistics column : columns) {
            addMissingValueInsight(column, insights);
            addOutlierInsight(column, insights);
            addLowDiversityInsight(column, insights);
            addWideRangeInsight(column, insights);
            addMixedTypeInsight(column, insights);
            addEmptyColumnInsight(column, insights);
        }

        if (insights.isEmpty()) {
            insights.add("No major data quality or distribution issues were detected by the current rules.");
        }

        return insights;
    }

    private void addMissingValueInsight(ColumnStatistics column, List<String> insights) {
        if (column.missingPercentage() >= 20.0) {
            insights.add("Column '" + column.columnName() + "' has "
                    + formatPercentage(column.missingPercentage()) + " missing values.");
        }
    }

    private void addOutlierInsight(ColumnStatistics column, List<String> insights) {
        if (column.outlierCount() > 0) {
            insights.add("Column '" + column.columnName() + "' contains "
                    + column.outlierCount() + " possible outliers.");
        }
    }

    private void addLowDiversityInsight(ColumnStatistics column, List<String> insights) {
        long presentValues = column.totalValues() - column.missingValues();
        if (presentValues >= 4
                && column.distinctCount() > 0
                && column.distinctCount() <= 2
                && column.inferredType() != ColumnType.EMPTY
                && column.inferredType() != ColumnType.NUMERIC) {
            insights.add("Column '" + column.columnName() + "' has low diversity: only "
                    + column.distinctCount() + " distinct values.");
        }
    }

    private void addWideRangeInsight(ColumnStatistics column, List<String> insights) {
        if (column.inferredType() != ColumnType.NUMERIC
                || column.min() == null
                || column.max() == null
                || column.mean() == null) {
            return;
        }

        double range = column.max() - column.min();
        double comparisonBase = Math.max(Math.abs(column.mean()), 1.0);
        if (range > comparisonBase * 5.0) {
            insights.add("Column '" + column.columnName() + "' appears to be numeric with a wide value range.");
        }
    }

    private void addMixedTypeInsight(ColumnStatistics column, List<String> insights) {
        if (column.inferredType() == ColumnType.MIXED) {
            insights.add("Column '" + column.columnName() + "' mixes numeric and text values.");
        }
    }

    private void addEmptyColumnInsight(ColumnStatistics column, List<String> insights) {
        if (column.inferredType() == ColumnType.EMPTY) {
            insights.add("Column '" + column.columnName() + "' is empty.");
        }
    }

    private String formatPercentage(double value) {
        if (value == Math.rint(value)) {
            return String.format("%.0f%%", value);
        }
        return String.format("%.1f%%", value);
    }
}
