package com.datasetanalytics.dataset.core.domain;

import java.time.Instant;
import java.util.List;

public record Dataset(
        Long id,
        String name,
        Instant createdAt,
        List<Column> columns,
        List<DataRow> rows,
        int rowCount
) {}
