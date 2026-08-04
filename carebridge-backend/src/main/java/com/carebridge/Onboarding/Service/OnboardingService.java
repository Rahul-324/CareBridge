package com.carebridge.Onboarding.Service;

import com.carebridge.Onboarding.dto.request.OnboardingRequest;
import com.carebridge.Onboarding.dto.response.OnboardingResponse;
import com.carebridge.auth.entity.User;
import com.carebridge.auth.entity.UserRole;
import com.carebridge.auth.repository.UserRepository;
import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.repository.ClinicRepository;
import com.carebridge.common.exception.DuplicateResourceException;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final TenantRepository tenantRepository;
    private final ClinicRepository clinicRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OnboardingResponse onboardClinic(
            OnboardingRequest request
    ) {
        String tenantEmail =
                normalizeEmail(request.tenant().email());

        String adminEmail =
                normalizeEmail(request.admin().email());

        validateDuplicateEmails(
                tenantEmail,
                adminEmail
        );

        String tenantCode = generateUniqueTenantCode();

        Tenant tenant = new Tenant(
                tenantCode,
                request.tenant().name().trim(),
                tenantEmail,
                normalizeNullable(request.tenant().phone())
        );

        Tenant savedTenant =
                tenantRepository.save(tenant);

        String clinicCode = generateUniqueClinicCode();

        Clinic clinic = new Clinic(
                savedTenant,
                clinicCode,
                request.clinic().clinicName().trim(),
                request.clinic().clinicType(),
                normalizeNullable(
                        request.clinic().registrationNumber()
                ),
                normalizeNullableEmail(
                        request.clinic().email()
                ),
                normalizeNullable(
                        request.clinic().phone()
                )
        );

        clinic.setWebsite(
                normalizeNullable(
                        request.clinic().website()
                )
        );

        Clinic savedClinic =
                clinicRepository.save(clinic);

        String branchCode =
                generateUniqueBranchCode(savedTenant.getId());

        Branch branch = new Branch(
                savedTenant,
                savedClinic,
                branchCode,
                request.branch().branchName().trim(),
                normalizeNullableEmail(
                        request.branch().email()
                ),
                normalizeNullable(
                        request.branch().phone()
                ),
                true
        );

        branch.setAddressLine1(
                request.branch().addressLine1().trim()
        );

        branch.setAddressLine2(
                normalizeNullable(
                        request.branch().addressLine2()
                )
        );

        branch.setCity(
                request.branch().city().trim()
        );

        branch.setState(
                request.branch().state().trim()
        );

        branch.setPostalCode(
                request.branch().postalCode().trim()
        );

        branch.setCountry(
                request.branch().country().trim()
        );

        Branch savedBranch =
                branchRepository.save(branch);

        String passwordHash =
                passwordEncoder.encode(
                        request.admin().password()
                );

        User adminUser = new User(
                savedTenant,
                savedBranch,
                request.admin().firstName().trim(),
                normalizeNullable(
                        request.admin().lastName()
                ),
                adminEmail,
                normalizeNullable(
                        request.admin().phone()
                ),
                passwordHash,
                UserRole.CLINIC_ADMIN
        );

        User savedAdmin =
                userRepository.save(adminUser);

        return new OnboardingResponse(
                savedTenant.getId(),
                savedTenant.getTenantCode(),
                savedTenant.getName(),

                savedClinic.getId(),
                savedClinic.getClinicCode(),
                savedClinic.getClinicName(),

                savedBranch.getId(),
                savedBranch.getBranchCode(),
                savedBranch.getBranchName(),

                savedAdmin.getId(),
                savedAdmin.getEmail()
        );
    }

    private void validateDuplicateEmails(
            String tenantEmail,
            String adminEmail
    ) {
        if (tenantRepository
                .existsByEmailIgnoreCase(tenantEmail)) {
            throw new DuplicateResourceException(
                    "A tenant already exists with email: "
                            + tenantEmail
            );
        }

        if (userRepository
                .existsByEmailIgnoreCase(adminEmail)) {
            throw new DuplicateResourceException(
                    "A user already exists with email: "
                            + adminEmail
            );
        }
    }

    private String generateUniqueTenantCode() {
        String tenantCode;

        do {
            tenantCode = "TEN-" + randomCode();
        } while (
                tenantRepository
                        .existsByTenantCode(tenantCode)
        );

        return tenantCode;
    }

    private String generateUniqueClinicCode() {
        String clinicCode;

        do {
            clinicCode = "CLN-" + randomCode();
        } while (
                clinicRepository
                        .existsByClinicCode(clinicCode)
        );

        return clinicCode;
    }

    private String generateUniqueBranchCode(
            UUID tenantId
    ) {
        String branchCode;

        do {
            branchCode = "BRN-" + randomCode();
        } while (
                branchRepository
                        .existsByTenant_IdAndBranchCode(
                                tenantId,
                                branchCode
                        )
        );

        return branchCode;
    }

    private String randomCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeNullableEmail(
            String email
    ) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return normalizeEmail(email);
    }

    private String normalizeNullable(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}