package com.carebridge.branch.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.branch.entity.Branch;

public interface BranchRepository extends JpaRepository<Branch,UUID>{
    Optional<Branch> findByTenant_IdAndBranchCode(
        UUID tenantId,
        String branchCode
    );
     List<Branch> findAllByTenant_Id(
            UUID tenantId
    );

    List<Branch> findAllByTenant_IdAndClinic_Id(
            UUID tenantId,
            UUID clinicId
    );

    Optional<Branch> findByTenant_IdAndClinic_IdAndMainBranchTrue(
            UUID tenantId,
            UUID clinicId
    );

    boolean existsByTenant_IdAndBranchCode(
            UUID tenantId,
            String branchCode
    );

    Optional<Branch> findByIdAndTenant_Id(UUID id, UUID tenantId);

}
