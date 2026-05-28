package com.datasetanalytics.dataset.adapters.out.persistence;

import com.datasetanalytics.dataset.core.domain.Column;
import com.datasetanalytics.dataset.core.domain.DataRow;
import com.datasetanalytics.dataset.core.domain.Dataset;
import com.datasetanalytics.dataset.core.port.out.DatasetRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PostgreSQLDatasetRepository implements DatasetRepository {
    private final SpringDataDatasetJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public PostgreSQLDatasetRepository(SpringDataDatasetJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Dataset save(Dataset dataset) {
        DatasetEntity entity = new DatasetEntity();
        entity.setName(dataset.name());
        if (dataset.createdAt() != null) {
            entity.setCreatedAt(dataset.createdAt());
        }

        for (int i = 0; i < dataset.columns().size(); i++) {
            Column column = dataset.columns().get(i);
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setDataset(entity);
            columnEntity.setName(column.name());
            columnEntity.setType(column.type());
            columnEntity.setPosition(i);
            entity.getColumns().add(columnEntity);
        }

        for (int i = 0; i < dataset.rows().size(); i++) {
            DataRow row = dataset.rows().get(i);
            DataRowEntity rowEntity = new DataRowEntity();
            rowEntity.setDataset(entity);
            rowEntity.setRowIndex(i);
            rowEntity.setValuesJson(writeJson(row.values()));
            entity.getRows().add(rowEntity);
        }

        DatasetEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dataset> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dataset> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainWithoutRows)
                .toList();
    }

    private Dataset toDomainWithoutRows(DatasetEntity entity) {
        List<Column> columns = entity.getColumns().stream()
                .map(column -> new Column(column.getName(), column.getType()))
                .toList();
        return new Dataset(entity.getId(), entity.getName(), entity.getCreatedAt(), columns, List.of(), entity.getRows().size());
    }

    private Dataset toDomain(DatasetEntity entity) {
        List<Column> columns = entity.getColumns().stream()
                .map(column -> new Column(column.getName(), column.getType()))
                .toList();

        List<DataRow> rows = entity.getRows().stream()
                .map(row -> new DataRow(readJson(row.getValuesJson())))
                .toList();

        return new Dataset(entity.getId(), entity.getName(), entity.getCreatedAt(), columns, rows, rows.size());
    }

    private String writeJson(Map<String, String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize row values", e);
        }
    }

    private Map<String, String> readJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize row values", e);
        }
    }
}
