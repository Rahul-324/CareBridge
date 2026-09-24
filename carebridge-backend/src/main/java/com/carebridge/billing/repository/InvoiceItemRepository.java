package com.carebridge.billing.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.billing.entity.InvoiceItem;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {
    List<InvoiceItem> findAllByInvoice_Id(UUID invoiceId);
}
