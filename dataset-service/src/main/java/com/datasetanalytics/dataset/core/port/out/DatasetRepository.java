package com.datasetanalytics.dataset.core.port.out;

import com.datasetanalytics.dataset.core.domain.Dataset;

import java.util.List;
import java.util.Optional;

public interface DatasetRepository {
    Dataset save(Dataset dataset);
    Optional<Dataset> findById(Long id);
    List<Dataset> findAll();
}
