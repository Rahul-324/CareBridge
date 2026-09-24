package com.carebridge.lab.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.lab.entity.LabTestCatalog;

public record LabTestResponse(
        UUID id,
        UUID tenantId,
        String testCode,
        String testName,
        String category,
        String description,
        BigDecimal price,
        String referenceRange,
        String turnaroundTime,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LabTestResponse from(LabTestCatalog catalog) {
        return new LabTestResponse(
                catalog.getId(),
                catalog.getTenant().getId(),
                catalog.getTestCode(),
                catalog.getTestName(),
                catalog.getCategory(),
                catalog.getDescription(),
                catalog.getPrice(),
                catalog.getReferenceRange(),
                catalog.getTurnaroundTime(),
                catalog.getStatus(),
                catalog.getCreatedAt(),
                catalog.getUpdatedAt()
        );
    }
}
