package com.example.dataset.adapters.in.web;

import com.example.dataset.core.domain.DataRow;
import com.example.dataset.core.domain.Dataset;
import com.example.dataset.core.port.in.CreateDatasetUseCase;
import com.example.dataset.core.port.out.DatasetRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class DatasetController {
    private final CreateDatasetUseCase createDatasetUseCase;
    private final DatasetRepository datasetRepository;

    public DatasetController(CreateDatasetUseCase createDatasetUseCase, DatasetRepository datasetRepository) {
        this.createDatasetUseCase = createDatasetUseCase;
        this.datasetRepository = datasetRepository;
    }

    @GetMapping("/datasets/ping")
    public Map<String, String> ping() {
        return Map.of("service", "dataset-service", "status", "ok");
    }

    @PostMapping("/datasets")
    public DatasetSummaryResponse create(@RequestBody CreateDatasetRequest request) {
        return toSummary(createDatasetUseCase.createEmptyDataset(request.name()));
    }

    @PostMapping(value = "/datasets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatasetSummaryResponse uploadCsv(@RequestParam String name, @RequestParam("file") MultipartFile file) throws IOException {
        String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        Dataset dataset = createDatasetUseCase.createDatasetFromCsv(name, csvContent);
        return toSummary(dataset);
    }

    @GetMapping("/datasets")
    public List<DatasetSummaryResponse> list() {
        return datasetRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/datasets/{datasetId}")
    public DatasetDetailsResponse get(@PathVariable Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));
        return toDetails(dataset);
    }

    @GetMapping("/internal/datasets/{datasetId}/data")
    public InternalDatasetDataResponse dataForAnalytics(@PathVariable Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        return new InternalDatasetDataResponse(
                dataset.id(),
                dataset.name(),
                dataset.columns().stream()
                        .map(column -> new ColumnResponse(column.name(), column.type()))
                        .toList(),
                dataset.rows().stream()
                        .map(DataRow::values)
                        .toList()
        );
    }

    @GetMapping("/internal/datasets/{datasetId}/rows")
    public List<Map<String, String>> rowsForAnalytics(@PathVariable Long datasetId) {
        return dataForAnalytics(datasetId).rows();
    }

    private DatasetSummaryResponse toSummary(Dataset dataset) {
        return new DatasetSummaryResponse(
                dataset.id(),
                dataset.name(),
                dataset.columns().size(),
                dataset.rowCount(),
                dataset.createdAt()
        );
    }

    private DatasetDetailsResponse toDetails(Dataset dataset) {
        return new DatasetDetailsResponse(
                dataset.id(),
                dataset.name(),
                dataset.columns().size(),
                dataset.rowCount(),
                dataset.createdAt(),
                dataset.columns().stream()
                        .map(column -> new ColumnResponse(column.name(), column.type()))
                        .toList()
        );
    }

    public record CreateDatasetRequest(String name) {}

    public record DatasetSummaryResponse(
            Long id,
            String name,
            int columnCount,
            int rowCount,
            Instant createdAt
    ) {}

    public record DatasetDetailsResponse(
            Long id,
            String name,
            int columnCount,
            int rowCount,
            Instant createdAt,
            List<ColumnResponse> columns
    ) {}

    public record ColumnResponse(String name, String type) {}

    public record InternalDatasetDataResponse(
            Long id,
            String name,
            List<ColumnResponse> columns,
            List<Map<String, String>> rows
    ) {}

    @ResponseStatus(code = org.springframework.http.HttpStatus.NOT_FOUND)
    public static class DatasetNotFoundException extends RuntimeException {
        public DatasetNotFoundException(Long datasetId) {
            super("Dataset not found: " + datasetId);
        }
    }
}
