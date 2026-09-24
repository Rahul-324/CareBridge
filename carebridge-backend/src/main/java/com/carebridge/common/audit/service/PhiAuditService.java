package com.carebridge.common.audit.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.common.audit.entity.PhiAuditLog;
import com.carebridge.common.audit.repository.PhiAuditLogRepository;
import com.carebridge.common.tenant.TenantContext;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhiAuditService {

    private final PhiAuditLogRepository phiAuditLogRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public void logAccess(String action, String resourceType, UUID resourceId, String details) {
        try {
            Optional<TenantContext> contextOpt = TenantContextHolder.get();
            if (contextOpt.isEmpty() || contextOpt.get().tenantId() == null) {
                return;
            }

            TenantContext context = contextOpt.get();
            UUID tenantId = context.tenantId();

            Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
            if (tenant == null) {
                return;
            }

            UUID userId = context.userId();
            String userEmail = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                userEmail = auth.getName();
            }

            PhiAuditLog auditLog = new PhiAuditLog(
                    tenant,
                    userId,
                    userEmail,
                    action,
                    resourceType,
                    resourceId,
                    details
            );

            phiAuditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.error("failed to log PHI audit entry: {}", ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public List<PhiAuditLog> getAuditLogsForTenant() {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return phiAuditLogRepository.findAllByTenant_IdOrderByTimestampDesc(tenantId);
    }
}
