package com.datasetanalytics.dataset.core.service;

import com.datasetanalytics.dataset.core.domain.Column;
import com.datasetanalytics.dataset.core.domain.DataRow;
import com.datasetanalytics.dataset.core.domain.Dataset;
import com.datasetanalytics.dataset.core.domain.DatasetValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvImporter {
    public Dataset importCsv(String name, String csvContent) {
        List<List<String>> records = parse(csvContent);
        if (records.isEmpty()) {
            throw new DatasetValidationException("CSV file is empty.");
        }

        List<String> headers = records.get(0).stream()
                .map(String::trim)
                .toList();

        if (headers.isEmpty() || headers.stream().anyMatch(String::isBlank)) {
            throw new DatasetValidationException("CSV header row must contain non-empty column names.");
        }

        if (records.size() == 1) {
            throw new DatasetValidationException("CSV file must contain at least one data row.");
        }

        List<Column> columns = headers.stream()
                .map(header -> new Column(header, "TEXT"))
                .toList();

        List<DataRow> rows = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            List<String> record = records.get(i);
            if (record.size() != headers.size()) {
                throw new DatasetValidationException("CSV row " + (i + 1) + " has "
                        + record.size() + " values but the header has "
                        + headers.size() + " columns.");
            }
            Map<String, String> values = new LinkedHashMap<>();
            for (int col = 0; col < headers.size(); col++) {
                String value = col < record.size() ? record.get(col) : "";
                values.put(headers.get(col), value);
            }
            rows.add(new DataRow(values));
        }

        return new Dataset(null, name, null, columns, rows, rows.size());
    }

    private List<List<String>> parse(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (ch == '"') {
                if (insideQuotes && i + 1 < content.length() && content.charAt(i + 1) == '"') {
                    currentCell.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (ch == ',' && !insideQuotes) {
                currentRow.add(currentCell.toString().trim());
                currentCell.setLength(0);
            } else if ((ch == '\n' || ch == '\r') && !insideQuotes) {
                if (ch == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                    i++;
                }
                currentRow.add(currentCell.toString().trim());
                currentCell.setLength(0);
                if (!isBlankRow(currentRow)) {
                    rows.add(currentRow);
                }
                currentRow = new ArrayList<>();
            } else {
                currentCell.append(ch);
            }
        }

        currentRow.add(currentCell.toString().trim());
        if (!isBlankRow(currentRow)) {
            rows.add(currentRow);
        }

        return rows;
    }

    private boolean isBlankRow(List<String> row) {
        return row.stream().allMatch(String::isBlank);
    }
}
