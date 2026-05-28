package com.datasetanalytics.analytics.core.service;

import com.datasetanalytics.analytics.core.domain.ColumnStatistics;
import com.datasetanalytics.analytics.core.domain.ColumnType;
import com.datasetanalytics.analytics.core.domain.DataQualityScore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataQualityScorer {
    public DataQualityScore score(List<ColumnStatistics> columns) {
        long totalCells = columns.stream()
                .mapToLong(ColumnStatistics::totalValues)
                .sum();

        if (totalCells == 0) {
            return new DataQualityScore(0, "F", "Dataset has no row values to evaluate.");
        }

        long missingValues = columns.stream()
                .mapToLong(ColumnStatistics::missingValues)
                .sum();
        long mixedColumns = columns.stream()
                .filter(column -> column.inferredType() == ColumnType.MIXED)
                .count();
        long emptyColumns = columns.stream()
                .filter(column -> column.inferredType() == ColumnType.EMPTY)
                .count();
        long outliers = columns.stream()
                .mapToLong(ColumnStatistics::outlierCount)
                .sum();
        long numericValues = columns.stream()
                .mapToLong(ColumnStatistics::numericValueCount)
                .sum();
        long columnsWithMissingValues = columns.stream()
                .filter(column -> column.missingValues() > 0)
                .count();
        long columnsWithSevereMissingValues = columns.stream()
                .filter(column -> column.missingPercentage() >= 20.0)
                .count();
        long columnsWithOutliers = columns.stream()
                .filter(column -> column.outlierCount() > 0)
                .count();
        long lowDiversityColumns = columns.stream()
                .filter(this::hasLowDiversity)
                .count();
        double maxMissingPercentage = columns.stream()
                .mapToDouble(ColumnStatistics::missingPercentage)
                .max()
                .orElse(0.0);

        double missingRatio = missingValues / (double) totalCells;
        double outlierDensity = numericValues == 0 ? 0.0 : outliers / (double) numericValues;
        long warningCount = columnsWithMissingValues
                + columnsWithSevereMissingValues
                + columnsWithOutliers
                + mixedColumns
                + emptyColumns
                + lowDiversityColumns;

        int penalty = (int) Math.round(missingRatio * 60);
        penalty += Math.min(30, mixedColumns * 12);
        penalty += Math.min(35, emptyColumns * 22);
        penalty += Math.min(25, (int) Math.round(outlierDensity * 60));
        penalty += Math.min(30, warningCount * 3);
        penalty += Math.min(20, (int) Math.round(maxMissingPercentage / 4.0));

        int score = Math.max(0, 100 - penalty);
        String grade = grade(score);
        String explanation = explanation(score, missingRatio, mixedColumns, outlierDensity, emptyColumns, warningCount);
        return new DataQualityScore(score, grade, explanation);
    }

    private String grade(int score) {
        if (score >= 90) {
            return "A";
        }
        if (score >= 75) {
            return "B";
        }
        if (score >= 60) {
            return "C";
        }
        if (score >= 40) {
            return "D";
        }
        return "F";
    }

    private String explanation(double score,
                               double missingRatio,
                               long mixedColumns,
                               double outlierDensity,
                               long emptyColumns,
                               long warningCount) {
        List<String> issues = new ArrayList<>();
        if (missingRatio >= 0.05) {
            issues.add("missing values");
        }
        if (mixedColumns > 0) {
            issues.add("mixed-type columns");
        }
        if (outlierDensity >= 0.02) {
            issues.add("possible outliers");
        }
        if (emptyColumns > 0) {
            issues.add("empty columns");
        }
        if (warningCount >= 4) {
            issues.add("repeated quality warnings");
        }

        if (issues.isEmpty()) {
            return "Dataset is complete and consistent based on the current checks.";
        }

        String issueText = joinIssues(issues);
        if (score >= 80) {
            return "Dataset is mostly complete but contains " + issueText + ".";
        }
        if (score >= 60) {
            return "Dataset needs review because it contains " + issueText + ".";
        }
        return "Dataset has significant quality issues including " + issueText + ".";
    }

    private boolean hasLowDiversity(ColumnStatistics column) {
        long presentValues = column.totalValues() - column.missingValues();
        return column.inferredType() != ColumnType.NUMERIC
                && column.inferredType() != ColumnType.EMPTY
                && presentValues >= 4
                && column.distinctCount() > 0
                && column.distinctCount() <= 2;
    }

    private String joinIssues(List<String> issues) {
        if (issues.size() == 1) {
            return issues.get(0);
        }

        return String.join(", ", issues.subList(0, issues.size() - 1))
                + " and "
                + issues.get(issues.size() - 1);
    }
}
