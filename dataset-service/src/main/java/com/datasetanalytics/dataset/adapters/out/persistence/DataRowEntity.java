package com.datasetanalytics.dataset.adapters.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "dataset_rows")
public class DataRowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private DatasetEntity dataset;

    @Column(nullable = false)
    private int rowIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valuesJson;

    public Long getId() { return id; }
    public DatasetEntity getDataset() { return dataset; }
    public void setDataset(DatasetEntity dataset) { this.dataset = dataset; }
    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    public String getValuesJson() { return valuesJson; }
    public void setValuesJson(String valuesJson) { this.valuesJson = valuesJson; }
}
