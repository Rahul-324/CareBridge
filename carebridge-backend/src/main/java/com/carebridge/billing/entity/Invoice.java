package com.carebridge.billing.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.carebridge.branch.entity.Branch;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.lab.entity.LabOrder;
import com.carebridge.patient.entity.Patient;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.visit.entity.Visit;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoices_tenant"))
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoices_clinic"))
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoices_branch"))
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoices_patient"))
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", foreignKey = @ForeignKey(name = "fk_invoices_visit"))
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", foreignKey = @ForeignKey(name = "fk_invoices_lab_order"))
    private LabOrder labOrder;

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Invoice(
            Tenant tenant,
            Clinic clinic,
            Branch branch,
            Patient patient,
            Visit visit,
            LabOrder labOrder,
            String invoiceNumber,
            LocalDateTime invoiceDate
    ) {
        this.tenant = tenant;
        this.clinic = clinic;
        this.branch = branch;
        this.patient = patient;
        this.visit = visit;
        this.labOrder = labOrder;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.status = InvoiceStatus.DRAFT;
    }

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
        recalculateTotals();
    }

    public void recalculateTotals() {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceItem item : items) {
            sum = sum.add(item.getTotalPrice());
        }
        this.subtotal = sum;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.totalAmount = sum.add(tax).subtract(discount);
        updateStatusBasedOnPayments();
    }

    public void updateStatusBasedOnPayments() {
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            if (this.status != InvoiceStatus.CANCELLED && this.status != InvoiceStatus.DRAFT) {
                this.status = InvoiceStatus.ISSUED;
            }
        } else if (paid.compareTo(totalAmount) >= 0) {
            this.status = InvoiceStatus.PAID;
        } else {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
