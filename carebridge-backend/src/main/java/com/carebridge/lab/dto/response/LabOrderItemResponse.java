package com.carebridge.lab.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.carebridge.lab.entity.LabOrderItem;
import com.carebridge.lab.entity.LabOrderStatus;

public record LabOrderItemResponse(
        UUID id,
        UUID testCatalogId,
        String testCode,
        String testName,
        LabOrderStatus status,
        String resultText,
        String referenceRange,
        String unit,
        String fileUrl,
        LocalDateTime performedAt,
        List<LabSampleResponse> samples,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LabOrderItemResponse from(LabOrderItem item) {
        return new LabOrderItemResponse(
                item.getId(),
                item.getTestCatalog().getId(),
                item.getTestCatalog().getTestCode(),
                item.getTestCatalog().getTestName(),
                item.getStatus(),
                item.getResultText(),
                item.getReferenceRange(),
                item.getUnit(),
                item.getFileUrl(),
                item.getPerformedAt(),
                item.getSamples().stream().map(LabSampleResponse::from).toList(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
