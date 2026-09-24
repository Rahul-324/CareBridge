package com.carebridge.lab.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.carebridge.lab.entity.LabOrder;
import com.carebridge.lab.entity.LabOrderStatus;

public record LabOrderResponse(
        UUID id,
        UUID tenantId,
        UUID clinicId,
        UUID branchId,
        String branchName,
        UUID patientId,
        String patientMrn,
        String patientFirstName,
        String patientLastName,
        UUID visitId,
        UUID doctorId,
        String doctorFirstName,
        String doctorLastName,
        String orderNumber,
        LocalDateTime orderDate,
        LabOrderStatus status,
        String notes,
        List<LabOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LabOrderResponse from(LabOrder order) {
        return new LabOrderResponse(
                order.getId(),
                order.getTenant().getId(),
                order.getClinic().getId(),
                order.getBranch().getId(),
                order.getBranch().getBranchName(),
                order.getPatient().getId(),
                order.getPatient().getMrn(),
                order.getPatient().getFirstName(),
                order.getPatient().getLastName(),
                order.getVisit() == null ? null : order.getVisit().getId(),
                order.getDoctor().getId(),
                order.getDoctor().getFirstName(),
                order.getDoctor().getLastName(),
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getStatus(),
                order.getNotes(),
                order.getItems().stream().map(LabOrderItemResponse::from).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
