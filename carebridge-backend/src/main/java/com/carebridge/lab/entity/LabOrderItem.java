package com.carebridge.lab.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lab_order_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabOrderItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lab_order_items_order"))
    private LabOrder labOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_catalog_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lab_order_items_catalog"))
    private LabTestCatalog testCatalog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LabOrderStatus status;

    @Column(name = "result_text", length = 2000)
    private String resultText;

    @Column(name = "reference_range", length = 255)
    private String referenceRange;

    @Column(length = 50)
    private String unit;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @OneToMany(mappedBy = "labOrderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabSample> samples = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LabOrderItem(LabOrder labOrder, LabTestCatalog testCatalog) {
        this.labOrder = labOrder;
        this.testCatalog = testCatalog;
        this.status = LabOrderStatus.PLACED;
    }

    public void addSample(LabSample sample) {
        samples.add(sample);
        sample.setLabOrderItem(this);
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = LabOrderStatus.PLACED;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
