package com.carebridge.lab.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.common.response.ApiResponse;
import com.carebridge.lab.dto.request.CreateLabTestRequest;
import com.carebridge.lab.dto.response.LabTestResponse;
import com.carebridge.lab.service.LabCatalogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lab/catalog")
@RequiredArgsConstructor
public class LabCatalogController {

    private final LabCatalogService catalogService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabTestResponse>> createTest(
            @Valid @RequestBody CreateLabTestRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("lab test catalog item created successfully", catalogService.createTest(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LabTestResponse>>> listCatalog(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab test catalog retrieved successfully", catalogService.listCatalog(status))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabTestResponse>> getTest(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab test catalog item retrieved successfully", catalogService.getTest(id))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabTestResponse>> updateTest(
            @PathVariable UUID id,
            @Valid @RequestBody CreateLabTestRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab test catalog item updated successfully", catalogService.updateTest(id, request))
        );
    }
}
