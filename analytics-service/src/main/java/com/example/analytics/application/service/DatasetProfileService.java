package com.example.analytics.application.service;

import com.example.analytics.core.domain.ColumnStatistics;
import com.example.analytics.core.domain.DataQualityScore;
import com.example.analytics.core.domain.DatasetData;
import com.example.analytics.core.domain.DatasetProfile;
import com.example.analytics.core.port.in.StatisticsAnalyzer;
import com.example.analytics.core.port.out.DatasetClient;
import com.example.analytics.core.service.DataQualityScorer;
import com.example.analytics.core.service.InsightGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetProfileService {
    private final DatasetClient datasetClient;
    private final StatisticsAnalyzer statisticsAnalyzer;
    private final DataQualityScorer dataQualityScorer;
    private final InsightGenerator insightGenerator;

    public DatasetProfileService(DatasetClient datasetClient,
                                 StatisticsAnalyzer statisticsAnalyzer,
                                 DataQualityScorer dataQualityScorer,
                                 InsightGenerator insightGenerator) {
        this.datasetClient = datasetClient;
        this.statisticsAnalyzer = statisticsAnalyzer;
        this.dataQualityScorer = dataQualityScorer;
        this.insightGenerator = insightGenerator;
    }

    public DatasetProfile profileDataset(Long datasetId) {
        DatasetData datasetData = datasetClient.fetchDatasetData(datasetId);
        List<ColumnStatistics> columns = statisticsAnalyzer.analyze(datasetData);
        DataQualityScore dataQuality = dataQualityScorer.score(columns);
        List<String> insights = insightGenerator.generate(columns);

        return new DatasetProfile(
                datasetData.id(),
                datasetData.name(),
                datasetData.rows().size(),
                datasetData.columns().size(),
                columns,
                dataQuality,
                insights
        );
    }

    public List<ColumnStatistics> summarizeDataset(Long datasetId) {
        return statisticsAnalyzer.analyze(datasetClient.fetchDatasetData(datasetId));
    }
}
