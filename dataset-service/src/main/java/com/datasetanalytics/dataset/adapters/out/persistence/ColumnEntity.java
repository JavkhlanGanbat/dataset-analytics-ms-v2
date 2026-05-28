package com.datasetanalytics.dataset.adapters.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "dataset_columns")
public class ColumnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private DatasetEntity dataset;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private int position;

    public Long getId() { return id; }
    public DatasetEntity getDataset() { return dataset; }
    public void setDataset(DatasetEntity dataset) { this.dataset = dataset; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
