package com.carebridge.patient.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.patient.dto.request.RegisterPatientRequest;
import com.carebridge.patient.dto.request.UpdatePatientRequest;
import com.carebridge.patient.dto.response.PatientResponse;
import com.carebridge.patient.entity.Patient;
import com.carebridge.patient.entity.PatientStatus;
import com.carebridge.patient.repository.PatientRepository;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public PatientResponse registerPatient(RegisterPatientRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));

        String mrn = generateUniqueMrn(tenantId);

        Patient patient = new Patient(
                tenant,
                mrn,
                request.firstName().trim(),
                normalizeNullable(request.lastName()),
                request.gender(),
                request.dateOfBirth(),
                request.phone().trim()
        );

        patient.setEmail(normalizeNullableEmail(request.email()));
        patient.setAddressLine1(normalizeNullable(request.addressLine1()));
        patient.setAddressLine2(normalizeNullable(request.addressLine2()));
        patient.setCity(normalizeNullable(request.city()));
        patient.setState(normalizeNullable(request.state()));
        patient.setPostalCode(normalizeNullable(request.postalCode()));
        patient.setCountry(normalizeNullable(request.country()));
        patient.setEmergencyContactName(normalizeNullable(request.emergencyContactName()));
        patient.setEmergencyContactPhone(normalizeNullable(request.emergencyContactPhone()));
        patient.setStatus(PatientStatus.ACTIVE);

        Patient saved = patientRepository.save(patient);
        return PatientResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatient(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Patient patient = patientRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));
        return PatientResponse.from(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(String query, PatientStatus status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        String searchQuery = query != null ? query.trim() : null;
        return patientRepository.searchPatients(tenantId, searchQuery, status).stream()
                .map(PatientResponse::from)
                .toList();
    }

    @Transactional
    public PatientResponse updatePatient(UUID id, UpdatePatientRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Patient patient = patientRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        patient.setFirstName(request.firstName().trim());
        patient.setLastName(normalizeNullable(request.lastName()));
        patient.setGender(request.gender());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setPhone(request.phone().trim());
        patient.setEmail(normalizeNullableEmail(request.email()));
        patient.setAddressLine1(normalizeNullable(request.addressLine1()));
        patient.setAddressLine2(normalizeNullable(request.addressLine2()));
        patient.setCity(normalizeNullable(request.city()));
        patient.setState(normalizeNullable(request.state()));
        patient.setPostalCode(normalizeNullable(request.postalCode()));
        patient.setCountry(normalizeNullable(request.country()));
        patient.setEmergencyContactName(normalizeNullable(request.emergencyContactName()));
        patient.setEmergencyContactPhone(normalizeNullable(request.emergencyContactPhone()));
        patient.setStatus(request.status());

        Patient saved = patientRepository.save(patient);
        return PatientResponse.from(saved);
    }

    private String generateUniqueMrn(UUID tenantId) {
        String mrn;
        do {
            mrn = "PAT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (patientRepository.existsByTenant_IdAndMrn(tenantId, mrn));
        return mrn;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeNullableEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
