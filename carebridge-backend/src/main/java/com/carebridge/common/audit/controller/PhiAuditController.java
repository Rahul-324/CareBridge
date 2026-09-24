package com.carebridge.common.audit.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.common.audit.entity.PhiAuditLog;
import com.carebridge.common.audit.service.PhiAuditService;
import com.carebridge.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit/phi")
@RequiredArgsConstructor
public class PhiAuditController {

    private final PhiAuditService phiAuditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<List<PhiAuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(
                ApiResponse.success("PHI audit logs retrieved successfully", phiAuditService.getAuditLogsForTenant())
        );
    }
}
