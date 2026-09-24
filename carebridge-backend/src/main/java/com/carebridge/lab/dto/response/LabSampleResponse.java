package com.carebridge.lab.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.lab.entity.LabSample;
import com.carebridge.lab.entity.SampleStatus;

public record LabSampleResponse(
        UUID id,
        UUID orderItemId,
        String sampleNumber,
        String sampleType,
        SampleStatus status,
        LocalDateTime collectedAt,
        LocalDateTime receivedAt,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LabSampleResponse from(LabSample sample) {
        return new LabSampleResponse(
                sample.getId(),
                sample.getLabOrderItem().getId(),
                sample.getSampleNumber(),
                sample.getSampleType(),
                sample.getStatus(),
                sample.getCollectedAt(),
                sample.getReceivedAt(),
                sample.getNotes(),
                sample.getCreatedAt(),
                sample.getUpdatedAt()
        );
    }
}
