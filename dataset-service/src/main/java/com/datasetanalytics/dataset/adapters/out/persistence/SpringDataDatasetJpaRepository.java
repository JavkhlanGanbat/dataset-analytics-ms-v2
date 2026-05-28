package com.datasetanalytics.dataset.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDatasetJpaRepository extends JpaRepository<DatasetEntity, Long> {
}
