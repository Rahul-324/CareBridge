package com.carebridge.lab.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.common.response.ApiResponse;
import com.carebridge.lab.dto.request.RecordResultRequest;
import com.carebridge.lab.dto.response.LabOrderItemResponse;
import com.carebridge.lab.service.LabSampleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lab/results")
@RequiredArgsConstructor
public class LabResultController {

    private final LabSampleService labSampleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'LAB_TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<LabOrderItemResponse>> recordResult(
            @Valid @RequestBody RecordResultRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab test result recorded successfully", labSampleService.recordResult(request))
        );
    }
}
