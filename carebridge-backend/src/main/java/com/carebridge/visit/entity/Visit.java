package com.carebridge.visit.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.appointment.entity.Appointment;
import com.carebridge.auth.entity.User;
import com.carebridge.branch.entity.Branch;
import com.carebridge.clinic.entity.Clinic;
import com.carebridge.patient.entity.Patient;
import com.carebridge.tenant.entity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Visit {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visits_tenant")
    )
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "clinic_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visits_clinic")
    )
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visits_branch")
    )
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visits_patient")
    )
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "doctor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visits_doctor")
    )
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "appointment_id",
            foreignKey = @ForeignKey(name = "fk_visits_appointment")
    )
    private Appointment appointment;

    @Column(name = "visit_number", nullable = false, length = 50)
    private String visitNumber;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "chief_complaint", length = 1000)
    private String chiefComplaint;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VisitStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Visit(
            Tenant tenant,
            Clinic clinic,
            Branch branch,
            Patient patient,
            User doctor,
            Appointment appointment,
            String visitNumber,
            LocalDateTime visitDate
    ) {
        this.tenant = tenant;
        this.clinic = clinic;
        this.branch = branch;
        this.patient = patient;
        this.doctor = doctor;
        this.appointment = appointment;
        this.visitNumber = visitNumber;
        this.visitDate = visitDate;
        this.status = VisitStatus.IN_PROGRESS;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = VisitStatus.IN_PROGRESS;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
