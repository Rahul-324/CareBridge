package com.carebridge.lab.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lab_samples")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabSample {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_order_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lab_samples_order_item"))
    private LabOrderItem labOrderItem;

    @Column(name = "sample_number", nullable = false, length = 50)
    private String sampleNumber;

    @Column(name = "sample_type", nullable = false, length = 50)
    private String sampleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SampleStatus status;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LabSample(
            LabOrderItem labOrderItem,
            String sampleNumber,
            String sampleType
    ) {
        this.labOrderItem = labOrderItem;
        this.sampleNumber = sampleNumber;
        this.sampleType = sampleType;
        this.status = SampleStatus.COLLECTED;
        this.collectedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = SampleStatus.COLLECTED;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
