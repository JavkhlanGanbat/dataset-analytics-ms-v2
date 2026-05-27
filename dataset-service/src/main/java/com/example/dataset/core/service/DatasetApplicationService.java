package com.example.dataset.core.service;

import com.example.dataset.core.domain.Dataset;
import com.example.dataset.core.port.in.CreateDatasetUseCase;
import com.example.dataset.core.port.out.DatasetRepository;
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
        return repository.save(new Dataset(null, name, null, List.of(), List.of(), 0));
    }

    @Override
    public Dataset createDatasetFromCsv(String name, String csvContent) {
        Dataset dataset = csvImporter.importCsv(name, csvContent);
        return repository.save(dataset);
    }
}
