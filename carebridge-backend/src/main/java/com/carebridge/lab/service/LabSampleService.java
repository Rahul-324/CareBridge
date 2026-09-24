package com.carebridge.lab.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.lab.dto.request.CollectSampleRequest;
import com.carebridge.lab.dto.request.RecordResultRequest;
import com.carebridge.lab.dto.request.UpdateSampleStatusRequest;
import com.carebridge.lab.dto.response.LabOrderItemResponse;
import com.carebridge.lab.dto.response.LabOrderResponse;
import com.carebridge.lab.dto.response.LabSampleResponse;
import com.carebridge.lab.entity.LabOrder;
import com.carebridge.lab.entity.LabOrderItem;
import com.carebridge.lab.entity.LabOrderStatus;
import com.carebridge.lab.entity.LabSample;
import com.carebridge.lab.entity.SampleStatus;
import com.carebridge.lab.repository.LabOrderItemRepository;
import com.carebridge.lab.repository.LabOrderRepository;
import com.carebridge.lab.repository.LabSampleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LabSampleService {

    private final LabSampleRepository sampleRepository;
    private final LabOrderItemRepository orderItemRepository;
    private final LabOrderRepository orderRepository;

    @Transactional
    public LabSampleResponse collectSample(CollectSampleRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        LabOrderItem orderItem = orderItemRepository.findByIdAndTenantId(request.orderItemId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("lab order item not found"));

        String sampleNumber = generateUniqueSampleNumber(tenantId);

        LabSample sample = new LabSample(
                orderItem,
                sampleNumber,
                request.sampleType().trim()
        );
        sample.setNotes(normalizeNullable(request.notes()));
        orderItem.addSample(sample);

        orderItem.setStatus(LabOrderStatus.SAMPLE_COLLECTED);

        LabOrder order = orderItem.getLabOrder();
        if (order.getStatus() == LabOrderStatus.PLACED) {
            order.setStatus(LabOrderStatus.SAMPLE_COLLECTED);
        }

        LabSample saved = sampleRepository.save(sample);
        return LabSampleResponse.from(saved);
    }

    @Transactional
    public LabSampleResponse updateSampleStatus(UUID sampleId, UpdateSampleStatusRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        LabSample sample = sampleRepository.findByIdAndTenantId(sampleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("lab sample not found"));

        sample.setStatus(request.status());
        if (request.status() == SampleStatus.RECEIVED && sample.getReceivedAt() == null) {
            sample.setReceivedAt(LocalDateTime.now());
            sample.getLabOrderItem().setStatus(LabOrderStatus.PROCESSING);
            sample.getLabOrderItem().getLabOrder().setStatus(LabOrderStatus.PROCESSING);
        }

        if (request.notes() != null && !request.notes().isBlank()) {
            sample.setNotes(request.notes().trim());
        }

        LabSample saved = sampleRepository.save(sample);
        return LabSampleResponse.from(saved);
    }

    @Transactional
    public LabOrderItemResponse recordResult(RecordResultRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        LabOrderItem orderItem = orderItemRepository.findByIdAndTenantId(request.orderItemId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("lab order item not found"));

        orderItem.setResultText(request.resultText().trim());
        if (request.referenceRange() != null && !request.referenceRange().isBlank()) {
            orderItem.setReferenceRange(request.referenceRange().trim());
        } else if (orderItem.getReferenceRange() == null && orderItem.getTestCatalog().getReferenceRange() != null) {
            orderItem.setReferenceRange(orderItem.getTestCatalog().getReferenceRange());
        }

        orderItem.setUnit(normalizeNullable(request.unit()));
        orderItem.setFileUrl(normalizeNullable(request.fileUrl()));
        orderItem.setPerformedAt(LocalDateTime.now());
        orderItem.setStatus(LabOrderStatus.COMPLETED);

        LabOrder order = orderItem.getLabOrder();
        boolean allCompleted = order.getItems().stream()
                .allMatch(item -> item.getStatus() == LabOrderStatus.COMPLETED);
        if (allCompleted) {
            order.setStatus(LabOrderStatus.COMPLETED);
        } else {
            order.setStatus(LabOrderStatus.PROCESSING);
        }

        LabOrderItem saved = orderItemRepository.save(orderItem);
        return LabOrderItemResponse.from(saved);
    }

    private String generateUniqueSampleNumber(UUID tenantId) {
        String number;
        do {
            number = "SMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (sampleRepository.existsByTenantIdAndSampleNumber(tenantId, number));
        return number;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
