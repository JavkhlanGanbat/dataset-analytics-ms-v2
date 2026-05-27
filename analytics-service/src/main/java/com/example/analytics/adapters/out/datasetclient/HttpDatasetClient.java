package com.example.analytics.adapters.out.datasetclient;

import com.example.analytics.core.domain.DatasetColumn;
import com.example.analytics.core.domain.DatasetData;
import com.example.analytics.core.port.out.DatasetClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class HttpDatasetClient implements DatasetClient {
    private final RestClient restClient;

    public HttpDatasetClient(RestClient.Builder builder,
                             @Value("${services.dataset.base-url}") String datasetBaseUrl) {
        this.restClient = builder.baseUrl(datasetBaseUrl).build();
    }

    @Override
    public DatasetData fetchDatasetData(Long datasetId) {
        InternalDatasetDataResponse response = restClient.get()
                .uri("/internal/datasets/{datasetId}/data", datasetId)
                .retrieve()
                .body(InternalDatasetDataResponse.class);

        if (response == null) {
            throw new IllegalStateException("Dataset Service returned an empty response for dataset " + datasetId);
        }

        List<DatasetColumn> columns = response.columns() == null
                ? List.of()
                : response.columns().stream()
                        .map(column -> new DatasetColumn(column.name(), column.type()))
                        .toList();
        List<Map<String, String>> rows = response.rows() == null ? List.of() : response.rows();

        return new DatasetData(response.id(), response.name(), columns, rows);
    }

    private record InternalDatasetDataResponse(
            Long id,
            String name,
            List<ColumnResponse> columns,
            List<Map<String, String>> rows
    ) {}

    private record ColumnResponse(String name, String type) {}
}
