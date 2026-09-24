package com.carebridge.lab.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.common.response.ApiResponse;
import com.carebridge.common.tenant.TenantContextHolder;

@RestController
@RequestMapping("/api/v1/lab/files")
public class LabFileController {

    public record FileUploadRequest(String fileName, String contentType) {}

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'LAB_TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestBody FileUploadRequest request
    ) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        String fileKey = UUID.randomUUID().toString();
        String fileUrl = "https://storage.carebridge.health/tenants/" + tenantId + "/lab-files/" + fileKey + "_" + (request.fileName() != null ? request.fileName() : "report.pdf");

        return ResponseEntity.ok(
                ApiResponse.success("file uploaded successfully", Map.of(
                        "fileUrl", fileUrl,
                        "fileKey", fileKey
                ))
        );
    }
}
