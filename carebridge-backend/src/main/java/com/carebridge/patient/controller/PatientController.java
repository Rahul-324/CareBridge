package com.carebridge.patient.controller;

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
import com.carebridge.patient.dto.request.RegisterPatientRequest;
import com.carebridge.patient.dto.request.UpdatePatientRequest;
import com.carebridge.patient.dto.response.PatientResponse;
import com.carebridge.patient.entity.PatientStatus;
import com.carebridge.patient.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PatientResponse>> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("patient registered successfully", patientService.registerPatient(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> searchPatients(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) PatientStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("patients retrieved successfully", patientService.searchPatients(query, status))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("patient retrieved successfully", patientService.getPatient(id))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("patient profile updated successfully", patientService.updatePatient(id, request))
        );
    }
}
