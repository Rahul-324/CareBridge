package com.carebridge.lab.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.common.response.ApiResponse;
import com.carebridge.lab.dto.request.CollectSampleRequest;
import com.carebridge.lab.dto.request.UpdateSampleStatusRequest;
import com.carebridge.lab.dto.response.LabSampleResponse;
import com.carebridge.lab.service.LabSampleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lab/samples")
@RequiredArgsConstructor
public class LabSampleController {

    private final LabSampleService labSampleService;

    @PostMapping("/collect")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'LAB_TECHNICIAN', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<LabSampleResponse>> collectSample(
            @Valid @RequestBody CollectSampleRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("lab sample collected successfully", labSampleService.collectSample(request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabSampleResponse>> updateSampleStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSampleStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab sample status updated successfully", labSampleService.updateSampleStatus(id, request))
        );
    }
}
