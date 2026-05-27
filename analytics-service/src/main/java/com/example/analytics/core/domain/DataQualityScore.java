package com.example.analytics.core.domain;

public record DataQualityScore(
        int score,
        String grade,
        String explanation
) {}
