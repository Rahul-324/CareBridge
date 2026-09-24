package com.carebridge.lab.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.common.exception.DuplicateResourceException;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.lab.dto.request.CreateLabTestRequest;
import com.carebridge.lab.dto.response.LabTestResponse;
import com.carebridge.lab.entity.LabTestCatalog;
import com.carebridge.lab.repository.LabTestCatalogRepository;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LabCatalogService {

    private final LabTestCatalogRepository catalogRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public LabTestResponse createTest(CreateLabTestRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        if (catalogRepository.existsByTenant_IdAndTestCode(tenantId, request.testCode().trim())) {
            throw new DuplicateResourceException("test code already exists in catalog");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));

        LabTestCatalog test = new LabTestCatalog(
                tenant,
                request.testCode().trim(),
                request.testName().trim(),
                normalizeNullable(request.category()),
                request.price()
        );

        test.setDescription(normalizeNullable(request.description()));
        test.setReferenceRange(normalizeNullable(request.referenceRange()));
        test.setTurnaroundTime(normalizeNullable(request.turnaroundTime()));

        LabTestCatalog saved = catalogRepository.save(test);
        return LabTestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<LabTestResponse> listCatalog(String status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        List<LabTestCatalog> list = (status != null && !status.isBlank())
                ? catalogRepository.findAllByTenant_IdAndStatus(tenantId, status.trim())
                : catalogRepository.findAllByTenant_Id(tenantId);

        return list.stream().map(LabTestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LabTestResponse getTest(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        LabTestCatalog test = catalogRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("test catalog item not found"));
        return LabTestResponse.from(test);
    }

    @Transactional
    public LabTestResponse updateTest(UUID id, CreateLabTestRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        LabTestCatalog test = catalogRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("test catalog item not found"));

        test.setTestName(request.testName().trim());
        test.setCategory(normalizeNullable(request.category()));
        test.setDescription(normalizeNullable(request.description()));
        test.setPrice(request.price());
        test.setReferenceRange(normalizeNullable(request.referenceRange()));
        test.setTurnaroundTime(normalizeNullable(request.turnaroundTime()));

        LabTestCatalog saved = catalogRepository.save(test);
        return LabTestResponse.from(saved);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
