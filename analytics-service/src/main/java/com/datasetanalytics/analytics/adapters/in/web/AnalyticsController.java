package com.datasetanalytics.analytics.adapters.in.web;

import com.datasetanalytics.analytics.application.service.DatasetProfileService;
import com.datasetanalytics.analytics.core.domain.ColumnStatistics;
import com.datasetanalytics.analytics.core.domain.DatasetProfile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AnalyticsController {
    private final DatasetProfileService datasetProfileService;

    public AnalyticsController(DatasetProfileService datasetProfileService) {
        this.datasetProfileService = datasetProfileService;
    }

    @GetMapping("/analytics/ping")
    public Map<String, String> ping() {
        return Map.of("service", "analytics-service", "status", "ok");
    }

    @GetMapping("/analytics/datasets/{datasetId}/summary")
    public List<ColumnStatistics> summarizeDataset(@PathVariable Long datasetId) {
        return datasetProfileService.summarizeDataset(datasetId);
    }

    @GetMapping("/analytics/datasets/{datasetId}/profile")
    public DatasetProfile profileDataset(@PathVariable Long datasetId) {
        return datasetProfileService.profileDataset(datasetId);
    }
}
