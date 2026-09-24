package com.carebridge.lab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.lab.entity.LabTestCatalog;

public interface LabTestCatalogRepository extends JpaRepository<LabTestCatalog, UUID> {

    Optional<LabTestCatalog> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndTestCode(UUID tenantId, String testCode);

    List<LabTestCatalog> findAllByTenant_Id(UUID tenantId);

    List<LabTestCatalog> findAllByTenant_IdAndStatus(UUID tenantId, String status);
}
