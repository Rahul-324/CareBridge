package com.carebridge.appointment.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.appointment.dto.request.BookAppointmentRequest;
import com.carebridge.appointment.dto.request.RescheduleAppointmentRequest;
import com.carebridge.appointment.dto.request.UpdateAppointmentStatusRequest;
import com.carebridge.appointment.dto.response.AppointmentResponse;
import com.carebridge.appointment.entity.AppointmentStatus;
import com.carebridge.appointment.service.AppointmentService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("appointment booked successfully", appointmentService.bookAppointment(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> listAppointments(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("appointments retrieved successfully", appointmentService.listAppointments(branchId, doctorId, patientId, status, startDate, endDate))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("appointment retrieved successfully", appointmentService.getAppointment(id))
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("appointment status updated successfully", appointmentService.updateAppointmentStatus(id, request))
        );
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("appointment rescheduled successfully", appointmentService.rescheduleAppointment(id, request))
        );
    }
}
