package com.carebridge.branch.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.entity.BranchStatus;

public record BranchResponse(
        UUID id,
        UUID tenantId,
        UUID clinicId,
        String branchCode,
        String branchName,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        boolean mainBranch,
        BranchStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getTenant().getId(),
                branch.getClinic().getId(),
                branch.getBranchCode(),
                branch.getBranchName(),
                branch.getEmail(),
                branch.getPhone(),
                branch.getAddressLine1(),
                branch.getAddressLine2(),
                branch.getCity(),
                branch.getState(),
                branch.getPostalCode(),
                branch.getCountry(),
                branch.isMainBranch(),
                branch.getStatus(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }
}
