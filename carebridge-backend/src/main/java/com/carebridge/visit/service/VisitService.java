package com.carebridge.visit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.appointment.entity.Appointment;
import com.carebridge.appointment.entity.AppointmentStatus;
import com.carebridge.appointment.repository.AppointmentRepository;
import com.carebridge.auth.entity.User;
import com.carebridge.auth.repository.UserRepository;
import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.entity.BranchStatus;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.repository.ClinicRepository;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.patient.entity.Patient;
import com.carebridge.patient.entity.PatientStatus;
import com.carebridge.patient.repository.PatientRepository;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;
import com.carebridge.visit.dto.request.CreateVisitRequest;
import com.carebridge.visit.dto.request.UpdateVisitRequest;
import com.carebridge.visit.dto.response.VisitResponse;
import com.carebridge.visit.entity.Visit;
import com.carebridge.visit.entity.VisitStatus;
import com.carebridge.visit.repository.VisitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final TenantRepository tenantRepository;
    private final ClinicRepository clinicRepository;
    private final BranchRepository branchRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public VisitResponse createVisit(CreateVisitRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));
        Clinic clinic = clinicRepository.findByTenant_Id(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("clinic not found"));

        Branch branch = branchRepository.findByIdAndTenant_Id(request.branchId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("branch not found"));
        if (branch.getStatus() != BranchStatus.ACTIVE) {
            throw new IllegalArgumentException("branch is not active");
        }

        Patient patient = patientRepository.findByIdAndTenant_Id(request.patientId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));
        if (patient.getStatus() != PatientStatus.ACTIVE) {
            throw new IllegalArgumentException("patient is not active");
        }

        User doctor = userRepository.findByIdAndTenant_Id(request.doctorId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findByIdAndTenant_Id(request.appointmentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("appointment not found"));
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        String visitNumber = generateUniqueVisitNumber(tenantId);
        LocalDateTime now = LocalDateTime.now();

        Visit visit = new Visit(
                tenant,
                clinic,
                branch,
                patient,
                doctor,
                appointment,
                visitNumber,
                now
        );

        visit.setChiefComplaint(normalizeNullable(request.chiefComplaint()));
        visit.setDiagnosis(normalizeNullable(request.diagnosis()));
        visit.setNotes(normalizeNullable(request.notes()));
        visit.setStatus(VisitStatus.IN_PROGRESS);

        Visit saved = visitRepository.save(visit);
        return VisitResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public VisitResponse getVisit(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Visit visit = visitRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("visit not found"));
        return VisitResponse.from(visit);
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> listVisits(UUID branchId, UUID doctorId, UUID patientId, VisitStatus status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return visitRepository.findVisits(tenantId, branchId, doctorId, patientId, status).stream()
                .map(VisitResponse::from)
                .toList();
    }

    @Transactional
    public VisitResponse updateVisit(UUID id, UpdateVisitRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Visit visit = visitRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("visit not found"));

        visit.setChiefComplaint(normalizeNullable(request.chiefComplaint()));
        visit.setDiagnosis(normalizeNullable(request.diagnosis()));
        visit.setNotes(normalizeNullable(request.notes()));
        visit.setStatus(request.status());

        Visit saved = visitRepository.save(visit);
        return VisitResponse.from(saved);
    }

    private String generateUniqueVisitNumber(UUID tenantId) {
        String number;
        do {
            number = "VST-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (visitRepository.existsByTenant_IdAndVisitNumber(tenantId, number));
        return number;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
