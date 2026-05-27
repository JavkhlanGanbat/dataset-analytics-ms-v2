package com.example.analytics.core.service;

import com.example.analytics.core.domain.ColumnStatistics;
import com.example.analytics.core.domain.ColumnType;
import com.example.analytics.core.domain.DatasetColumn;
import com.example.analytics.core.domain.DatasetData;
import com.example.analytics.core.port.in.StatisticsAnalyzer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InMemoryStatisticsAnalyzer implements StatisticsAnalyzer {
    @Override
    public List<ColumnStatistics> analyze(DatasetData datasetData) {
        List<Map<String, String>> rows = datasetData.rows() == null ? List.of() : datasetData.rows();
        List<String> columnNames = resolveColumnNames(datasetData, rows);

        List<ColumnStatistics> result = new ArrayList<>();
        for (String columnName : columnNames) {
            result.add(analyzeColumn(columnName, rows));
        }

        return result;
    }

    private List<String> resolveColumnNames(DatasetData datasetData, List<Map<String, String>> rows) {
        if (datasetData.columns() != null && !datasetData.columns().isEmpty()) {
            return datasetData.columns().stream()
                    .map(DatasetColumn::name)
                    .toList();
        }

        Set<String> columnNames = new LinkedHashSet<>();
        rows.forEach(row -> columnNames.addAll(row.keySet()));
        return List.copyOf(columnNames);
    }

    private ColumnStatistics analyzeColumn(String columnName, List<Map<String, String>> rows) {
        List<String> nonMissingValues = new ArrayList<>();
        List<String> textValues = new ArrayList<>();
        List<Double> numericValues = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String rawValue = row.get(columnName);
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }

            String value = rawValue.trim();
            nonMissingValues.add(value);

            Double numericValue = parseNumber(value);
            if (numericValue == null) {
                textValues.add(value);
            } else {
                numericValues.add(numericValue);
            }
        }

        long totalValues = rows.size();
        long missingValues = totalValues - nonMissingValues.size();
        ColumnType inferredType = inferType(nonMissingValues.size(), numericValues.size());
        double missingPercentage = totalValues == 0 ? 0.0 : round(missingValues * 100.0 / totalValues, 2);
        long distinctCount = nonMissingValues.stream().collect(Collectors.toSet()).size();

        NumericSummary numericSummary = summarizeNumbers(numericValues);
        Map<String, Long> mostCommonValues = mostCommonValues(inferredType, nonMissingValues);
        Double averageTextLength = averageTextLength(inferredType, textValues, nonMissingValues);

        return new ColumnStatistics(
                columnName,
                inferredType,
                totalValues,
                missingValues,
                missingPercentage,
                distinctCount,
                numericValues.size(),
                numericSummary.min(),
                numericSummary.max(),
                numericSummary.mean(),
                numericSummary.median(),
                numericSummary.standardDeviation(),
                numericSummary.outlierCount(),
                mostCommonValues,
                averageTextLength
        );
    }

    private ColumnType inferType(int nonMissingCount, int numericCount) {
        if (nonMissingCount == 0) {
            return ColumnType.EMPTY;
        }
        if (numericCount == nonMissingCount) {
            return ColumnType.NUMERIC;
        }
        if (numericCount == 0) {
            return ColumnType.TEXT;
        }
        return ColumnType.MIXED;
    }

    private Double parseNumber(String value) {
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private NumericSummary summarizeNumbers(List<Double> values) {
        if (values.isEmpty()) {
            return new NumericSummary(null, null, null, null, null, 0);
        }

        List<Double> sorted = values.stream()
                .sorted()
                .toList();

        double min = sorted.get(0);
        double max = sorted.get(sorted.size() - 1);
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double median = percentile(sorted, 50);
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);

        return new NumericSummary(
                round(min, 4),
                round(max, 4),
                round(mean, 4),
                round(median, 4),
                round(Math.sqrt(variance), 4),
                countOutliers(sorted)
        );
    }

    private long countOutliers(List<Double> sortedValues) {
        if (sortedValues.size() < 4) {
            return 0;
        }

        double firstQuartile = percentile(sortedValues, 25);
        double thirdQuartile = percentile(sortedValues, 75);
        double iqr = thirdQuartile - firstQuartile;
        if (iqr == 0.0) {
            return 0;
        }

        double lowerFence = firstQuartile - 1.5 * iqr;
        double upperFence = thirdQuartile + 1.5 * iqr;
        return sortedValues.stream()
                .filter(value -> value < lowerFence || value > upperFence)
                .count();
    }

    private double percentile(List<Double> sortedValues, double percentile) {
        if (sortedValues.size() == 1) {
            return sortedValues.get(0);
        }

        double index = percentile / 100.0 * (sortedValues.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }

        double weight = index - lowerIndex;
        return sortedValues.get(lowerIndex) * (1.0 - weight) + sortedValues.get(upperIndex) * weight;
    }

    private Map<String, Long> mostCommonValues(ColumnType inferredType, List<String> nonMissingValues) {
        if (inferredType == ColumnType.NUMERIC || nonMissingValues.isEmpty()) {
            return Map.of();
        }

        return nonMissingValues.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Double averageTextLength(ColumnType inferredType, List<String> textValues, List<String> nonMissingValues) {
        List<String> valuesToMeasure;
        if (inferredType == ColumnType.TEXT) {
            valuesToMeasure = nonMissingValues;
        } else if (inferredType == ColumnType.MIXED) {
            valuesToMeasure = textValues;
        } else {
            return null;
        }

        if (valuesToMeasure.isEmpty()) {
            return null;
        }

        double average = valuesToMeasure.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);
        return round(average, 2);
    }

    private double round(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }

    private record NumericSummary(
            Double min,
            Double max,
            Double mean,
            Double median,
            Double standardDeviation,
            long outlierCount
    ) {
    }
}
