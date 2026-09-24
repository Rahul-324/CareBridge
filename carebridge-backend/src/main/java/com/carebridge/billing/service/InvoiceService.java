package com.carebridge.billing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.billing.dto.request.CreateInvoiceItemRequest;
import com.carebridge.billing.dto.request.CreateInvoiceRequest;
import com.carebridge.billing.dto.request.GenerateInvoiceFromLabOrderRequest;
import com.carebridge.billing.dto.request.GenerateInvoiceFromVisitRequest;
import com.carebridge.billing.dto.response.InvoiceResponse;
import com.carebridge.billing.entity.Invoice;
import com.carebridge.billing.entity.InvoiceItem;
import com.carebridge.billing.entity.InvoiceStatus;
import com.carebridge.billing.entity.ItemType;
import com.carebridge.billing.repository.InvoiceRepository;
import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.repository.ClinicRepository;
import com.carebridge.common.exception.DuplicateResourceException;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.lab.entity.LabOrder;
import com.carebridge.lab.entity.LabOrderItem;
import com.carebridge.lab.repository.LabOrderRepository;
import com.carebridge.patient.entity.Patient;
import com.carebridge.patient.repository.PatientRepository;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;
import com.carebridge.visit.entity.Visit;
import com.carebridge.visit.repository.VisitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TenantRepository tenantRepository;
    private final ClinicRepository clinicRepository;
    private final BranchRepository branchRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final LabOrderRepository labOrderRepository;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));
        Clinic clinic = clinicRepository.findByTenant_Id(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("clinic not found"));
        Branch branch = branchRepository.findByIdAndTenant_Id(request.branchId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("branch not found"));
        Patient patient = patientRepository.findByIdAndTenant_Id(request.patientId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        Visit visit = null;
        if (request.visitId() != null) {
            visit = visitRepository.findByIdAndTenant_Id(request.visitId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("visit not found"));
        }

        LabOrder labOrder = null;
        if (request.labOrderId() != null) {
            labOrder = labOrderRepository.findByIdAndTenant_Id(request.labOrderId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("lab order not found"));
        }

        String invoiceNumber = generateUniqueInvoiceNumber(tenantId);
        LocalDateTime now = LocalDateTime.now();

        Invoice invoice = new Invoice(
                tenant,
                clinic,
                branch,
                patient,
                visit,
                labOrder,
                invoiceNumber,
                now
        );

        if (request.dueDate() != null) {
            invoice.setDueDate(request.dueDate());
        } else {
            invoice.setDueDate(now.plusDays(30));
        }

        invoice.setTaxAmount(request.taxAmount() != null ? request.taxAmount() : BigDecimal.ZERO);
        invoice.setDiscountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO);
        invoice.setNotes(normalizeNullable(request.notes()));
        invoice.setStatus(InvoiceStatus.ISSUED);

        for (CreateInvoiceItemRequest itemReq : request.items()) {
            InvoiceItem item = new InvoiceItem(
                    invoice,
                    itemReq.itemName().trim(),
                    itemReq.itemType(),
                    itemReq.unitPrice(),
                    itemReq.quantity()
            );
            invoice.addItem(item);
        }

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponse.from(saved);
    }

    @Transactional
    public InvoiceResponse generateFromVisit(GenerateInvoiceFromVisitRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        visitRepository.findByIdAndTenant_Id(request.visitId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("visit not found"));

        if (invoiceRepository.findByTenant_IdAndVisit_Id(tenantId, request.visitId()).isPresent()) {
            throw new DuplicateResourceException("invoice already generated for this visit");
        }

        Visit visit = visitRepository.findByIdAndTenant_Id(request.visitId(), tenantId).get();

        BigDecimal fee = request.consultationFee() != null ? request.consultationFee() : new BigDecimal("500.00");
        BigDecimal discount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;

        String invoiceNumber = generateUniqueInvoiceNumber(tenantId);
        LocalDateTime now = LocalDateTime.now();

        Invoice invoice = new Invoice(
                visit.getTenant(),
                visit.getClinic(),
                visit.getBranch(),
                visit.getPatient(),
                visit,
                null,
                invoiceNumber,
                now
        );

        invoice.setDueDate(now.plusDays(14));
        invoice.setDiscountAmount(discount);
        invoice.setNotes(normalizeNullable(request.notes()));
        invoice.setStatus(InvoiceStatus.ISSUED);

        InvoiceItem consultationItem = new InvoiceItem(
                invoice,
                "Doctor Consultation Fee",
                ItemType.CONSULTATION,
                fee,
                1
        );
        invoice.addItem(consultationItem);

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponse.from(saved);
    }

    @Transactional
    public InvoiceResponse generateFromLabOrder(GenerateInvoiceFromLabOrderRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        LabOrder labOrder = labOrderRepository.findByIdAndTenant_Id(request.labOrderId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("lab order not found"));

        if (invoiceRepository.findByTenant_IdAndLabOrder_Id(tenantId, request.labOrderId()).isPresent()) {
            throw new DuplicateResourceException("invoice already generated for this lab order");
        }

        BigDecimal discount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;

        String invoiceNumber = generateUniqueInvoiceNumber(tenantId);
        LocalDateTime now = LocalDateTime.now();

        Invoice invoice = new Invoice(
                labOrder.getTenant(),
                labOrder.getClinic(),
                labOrder.getBranch(),
                labOrder.getPatient(),
                labOrder.getVisit(),
                labOrder,
                invoiceNumber,
                now
        );

        invoice.setDueDate(now.plusDays(14));
        invoice.setDiscountAmount(discount);
        invoice.setNotes(normalizeNullable(request.notes()));
        invoice.setStatus(InvoiceStatus.ISSUED);

        for (LabOrderItem orderItem : labOrder.getItems()) {
            BigDecimal testPrice = orderItem.getTestCatalog().getPrice();
            InvoiceItem item = new InvoiceItem(
                    invoice,
                    orderItem.getTestCatalog().getTestName(),
                    ItemType.LAB_TEST,
                    testPrice,
                    1
            );
            invoice.addItem(item);
        }

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("invoice not found"));
        return InvoiceResponse.from(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoices(UUID patientId, UUID visitId, UUID labOrderId, InvoiceStatus status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return invoiceRepository.findInvoices(tenantId, patientId, visitId, labOrderId, status).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Transactional
    public InvoiceResponse updateInvoiceStatus(UUID id, InvoiceStatus status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("invoice not found"));

        invoice.setStatus(status);
        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponse.from(saved);
    }

    private String generateUniqueInvoiceNumber(UUID tenantId) {
        String number;
        do {
            number = "INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (invoiceRepository.existsByTenant_IdAndInvoiceNumber(tenantId, number));
        return number;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
