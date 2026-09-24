package com.carebridge.lab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.auth.entity.User;
import com.carebridge.auth.repository.UserRepository;
import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.repository.ClinicRepository;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.lab.dto.request.CreateLabOrderRequest;
import com.carebridge.lab.dto.response.LabOrderResponse;
import com.carebridge.lab.entity.LabOrder;
import com.carebridge.lab.entity.LabOrderItem;
import com.carebridge.lab.entity.LabOrderStatus;
import com.carebridge.lab.entity.LabTestCatalog;
import com.carebridge.lab.repository.LabOrderRepository;
import com.carebridge.lab.repository.LabTestCatalogRepository;
import com.carebridge.patient.entity.Patient;
import com.carebridge.patient.repository.PatientRepository;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;
import com.carebridge.visit.entity.Visit;
import com.carebridge.visit.repository.VisitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final LabTestCatalogRepository catalogRepository;
    private final TenantRepository tenantRepository;
    private final ClinicRepository clinicRepository;
    private final BranchRepository branchRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;

    @Transactional
    public LabOrderResponse createOrder(CreateLabOrderRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));
        Clinic clinic = clinicRepository.findByTenant_Id(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("clinic not found"));
        Branch branch = branchRepository.findByIdAndTenant_Id(request.branchId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("branch not found"));
        Patient patient = patientRepository.findByIdAndTenant_Id(request.patientId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));
        User doctor = userRepository.findByIdAndTenant_Id(request.doctorId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));

        Visit visit = null;
        if (request.visitId() != null) {
            visit = visitRepository.findByIdAndTenant_Id(request.visitId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("visit not found"));
        }

        String orderNumber = generateUniqueOrderNumber(tenantId);
        LocalDateTime now = LocalDateTime.now();

        LabOrder order = new LabOrder(
                tenant,
                clinic,
                branch,
                patient,
                visit,
                doctor,
                orderNumber,
                now
        );

        order.setNotes(normalizeNullable(request.notes()));

        for (UUID testCatalogId : request.testCatalogIds()) {
            LabTestCatalog catalogItem = catalogRepository.findByIdAndTenant_Id(testCatalogId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("test catalog item not found: " + testCatalogId));
            LabOrderItem item = new LabOrderItem(order, catalogItem);
            order.addItem(item);
        }

        LabOrder saved = labOrderRepository.save(order);
        return LabOrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LabOrderResponse getOrder(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        LabOrder order = labOrderRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("lab order not found"));
        return LabOrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> listOrders(UUID patientId, UUID visitId, LabOrderStatus status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return labOrderRepository.findOrders(tenantId, patientId, visitId, status).stream()
                .map(LabOrderResponse::from)
                .toList();
    }

    @Transactional
    public LabOrderResponse updateOrderStatus(UUID id, LabOrderStatus status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        LabOrder order = labOrderRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("lab order not found"));

        order.setStatus(status);
        for (LabOrderItem item : order.getItems()) {
            item.setStatus(status);
        }

        LabOrder saved = labOrderRepository.save(order);
        return LabOrderResponse.from(saved);
    }

    private String generateUniqueOrderNumber(UUID tenantId) {
        String number;
        do {
            number = "LBD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (labOrderRepository.existsByTenant_IdAndOrderNumber(tenantId, number));
        return number;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
