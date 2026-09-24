package com.carebridge.visit.controller;

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
import com.carebridge.visit.dto.request.CreateVisitRequest;
import com.carebridge.visit.dto.request.UpdateVisitRequest;
import com.carebridge.visit.dto.response.VisitResponse;
import com.carebridge.visit.entity.VisitStatus;
import com.carebridge.visit.service.VisitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<VisitResponse>> createVisit(
            @Valid @RequestBody CreateVisitRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("visit created successfully", visitService.createVisit(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VisitResponse>>> listVisits(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) VisitStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("visits retrieved successfully", visitService.listVisits(branchId, doctorId, patientId, status))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VisitResponse>> getVisit(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("visit retrieved successfully", visitService.getVisit(id))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<VisitResponse>> updateVisit(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVisitRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("visit updated successfully", visitService.updateVisit(id, request))
        );
    }
}
