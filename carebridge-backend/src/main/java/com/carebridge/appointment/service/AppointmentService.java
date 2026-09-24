package com.carebridge.appointment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.appointment.dto.request.BookAppointmentRequest;
import com.carebridge.appointment.dto.request.RescheduleAppointmentRequest;
import com.carebridge.appointment.dto.request.UpdateAppointmentStatusRequest;
import com.carebridge.appointment.dto.response.AppointmentResponse;
import com.carebridge.appointment.entity.Appointment;
import com.carebridge.appointment.entity.AppointmentStatus;
import com.carebridge.appointment.repository.AppointmentRepository;
import com.carebridge.auth.entity.User;
import com.carebridge.auth.entity.UserRole;
import com.carebridge.auth.entity.UserStatus;
import com.carebridge.auth.repository.UserRepository;
import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.entity.BranchStatus;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.repository.ClinicRepository;
import com.carebridge.common.exception.DuplicateResourceException;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.patient.entity.Patient;
import com.carebridge.patient.entity.PatientStatus;
import com.carebridge.patient.repository.PatientRepository;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TenantRepository tenantRepository;
    private final ClinicRepository clinicRepository;
    private final BranchRepository branchRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("appointment start time must be before end time");
        }

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
        if (doctor.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("doctor is not active");
        }
        if (doctor.getRole() != UserRole.DOCTOR && doctor.getRole() != UserRole.CLINIC_ADMIN) {
            throw new IllegalArgumentException("assigned user is not a valid healthcare provider");
        }

        validateNoConflicts(tenantId, request.doctorId(), request.patientId(), request.startTime(), request.endTime(), null);

        String appointmentNumber = generateUniqueAppointmentNumber(tenantId);

        Appointment appointment = new Appointment(
                tenant,
                clinic,
                branch,
                patient,
                doctor,
                appointmentNumber,
                request.appointmentType(),
                request.startTime(),
                request.endTime(),
                normalizeNullable(request.reasonForVisit())
        );

        appointment.setNotes(normalizeNullable(request.notes()));
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Appointment appointment = appointmentRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("appointment not found"));
        return AppointmentResponse.from(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listAppointments(
            UUID branchId,
            UUID doctorId,
            UUID patientId,
            AppointmentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return appointmentRepository.findAppointments(tenantId, branchId, doctorId, patientId, status, startDate, endDate).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(UUID id, UpdateAppointmentStatusRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Appointment appointment = appointmentRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("appointment not found"));

        appointment.setStatus(request.status());
        if (request.notes() != null && !request.notes().isBlank()) {
            appointment.setNotes(request.notes().trim());
        }

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.from(saved);
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(UUID id, RescheduleAppointmentRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Appointment appointment = appointmentRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("cannot reschedule a completed or cancelled appointment");
        }

        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("appointment start time must be before end time");
        }

        validateNoConflicts(tenantId, appointment.getDoctor().getId(), appointment.getPatient().getId(), request.startTime(), request.endTime(), id);

        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        if (request.notes() != null && !request.notes().isBlank()) {
            appointment.setNotes(request.notes().trim());
        }

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.from(saved);
    }

    private void validateNoConflicts(
            UUID tenantId,
            UUID doctorId,
            UUID patientId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            UUID excludeId
    ) {
        if (appointmentRepository.hasDoctorConflict(tenantId, doctorId, startTime, endTime, excludeId)) {
            throw new DuplicateResourceException("doctor has a conflicting appointment during this time slot");
        }

        if (appointmentRepository.hasPatientConflict(tenantId, patientId, startTime, endTime, excludeId)) {
            throw new DuplicateResourceException("patient has a conflicting appointment during this time slot");
        }
    }

    private String generateUniqueAppointmentNumber(UUID tenantId) {
        String number;
        do {
            number = "APT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (appointmentRepository.existsByTenant_IdAndAppointmentNumber(tenantId, number));
        return number;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
