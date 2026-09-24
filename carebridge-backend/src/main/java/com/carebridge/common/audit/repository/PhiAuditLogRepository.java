package com.carebridge.common.audit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.common.audit.entity.PhiAuditLog;

public interface PhiAuditLogRepository extends JpaRepository<PhiAuditLog, UUID> {
    List<PhiAuditLog> findAllByTenant_IdOrderByTimestampDesc(UUID tenantId);
}
