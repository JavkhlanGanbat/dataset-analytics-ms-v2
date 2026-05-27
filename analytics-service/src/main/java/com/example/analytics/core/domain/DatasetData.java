package com.example.analytics.core.domain;

import java.util.List;
import java.util.Map;

public record DatasetData(
        Long id,
        String name,
        List<DatasetColumn> columns,
        List<Map<String, String>> rows
) {}
