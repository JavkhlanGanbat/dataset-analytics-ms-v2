package com.datasetanalytics.dataset.core.service;

import com.datasetanalytics.dataset.core.domain.Dataset;
import com.datasetanalytics.dataset.core.domain.DatasetValidationException;
import com.datasetanalytics.dataset.core.port.in.CreateDatasetUseCase;
import com.datasetanalytics.dataset.core.port.out.DatasetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetApplicationService implements CreateDatasetUseCase {
    private final DatasetRepository repository;
    private final CsvImporter csvImporter = new CsvImporter();

    public DatasetApplicationService(DatasetRepository repository) {
        this.repository = repository;
    }

    @Override
    public Dataset createEmptyDataset(String name) {
        validateDatasetName(name);
        return repository.save(new Dataset(null, name.trim(), null, List.of(), List.of(), 0));
    }

    @Override
    public Dataset createDatasetFromCsv(String name, String csvContent) {
        validateDatasetName(name);
        Dataset dataset = csvImporter.importCsv(name.trim(), csvContent);
        return repository.save(dataset);
    }

    private void validateDatasetName(String name) {
        if (name == null || name.isBlank()) {
            throw new DatasetValidationException("Dataset name is required.");
        }
    }
}
