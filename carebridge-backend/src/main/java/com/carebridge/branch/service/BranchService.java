package com.carebridge.branch.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.branch.dto.request.CreateBranchRequest;
import com.carebridge.branch.dto.request.UpdateBranchStatusRequest;
import com.carebridge.branch.dto.response.BranchResponse;
import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.entity.BranchStatus;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.repository.ClinicRepository;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final ClinicRepository clinicRepository;

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));
        Clinic clinic = clinicRepository.findByTenant_Id(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("clinic not found"));

        String branchCode = generateUniqueBranchCode(tenantId);

        Branch branch = new Branch(
                tenant,
                clinic,
                branchCode,
                request.branchName().trim(),
                normalizeNullableEmail(request.email()),
                normalizeNullable(request.phone()),
                false
        );

        branch.setAddressLine1(request.addressLine1().trim());
        branch.setAddressLine2(normalizeNullable(request.addressLine2()));
        branch.setCity(request.city().trim());
        branch.setState(request.state().trim());
        branch.setPostalCode(request.postalCode().trim());
        branch.setCountry(request.country().trim());
        branch.setStatus(BranchStatus.ACTIVE);

        Branch savedBranch = branchRepository.save(branch);
        return BranchResponse.from(savedBranch);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> listBranches() {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return branchRepository.findAllByTenant_Id(tenantId).stream()
                .map(BranchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranch(UUID branchId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Branch branch = branchRepository.findByIdAndTenant_Id(branchId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("branch not found"));
        return BranchResponse.from(branch);
    }

    @Transactional
    public BranchResponse updateBranchStatus(UUID branchId, UpdateBranchStatusRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Branch branch = branchRepository.findByIdAndTenant_Id(branchId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("branch not found"));

        if (branch.isMainBranch() && request.status() == BranchStatus.INACTIVE) {
            throw new IllegalArgumentException("cannot deactivate main branch");
        }

        branch.setStatus(request.status());
        Branch updatedBranch = branchRepository.save(branch);
        return BranchResponse.from(updatedBranch);
    }

    private String generateUniqueBranchCode(UUID tenantId) {
        String branchCode;
        do {
            branchCode = "BRN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (branchRepository.existsByTenant_IdAndBranchCode(tenantId, branchCode));
        return branchCode;
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
