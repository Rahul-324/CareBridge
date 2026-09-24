package com.carebridge.clinic.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.clinic.dto.request.UpdateClinicRequest;
import com.carebridge.clinic.dto.response.ClinicResponse;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.repository.ClinicRepository;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    @Transactional(readOnly = true)
    public ClinicResponse getClinicProfile() {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Clinic clinic = clinicRepository.findByTenant_Id(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("clinic profile not found"));
        return ClinicResponse.from(clinic);
    }

    @Transactional
    public ClinicResponse updateClinicProfile(UpdateClinicRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Clinic clinic = clinicRepository.findByTenant_Id(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("clinic profile not found"));

        clinic.setClinicName(request.clinicName().trim());
        clinic.setClinicType(request.clinicType());
        clinic.setRegistrationNumber(normalizeNullable(request.registrationNumber()));
        clinic.setEmail(normalizeNullableEmail(request.email()));
        clinic.setPhone(normalizeNullable(request.phone()));
        clinic.setWebsite(normalizeNullable(request.website()));
        clinic.setLogoUrl(normalizeNullable(request.logoUrl()));

        return ClinicResponse.from(clinicRepository.save(clinic));
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
