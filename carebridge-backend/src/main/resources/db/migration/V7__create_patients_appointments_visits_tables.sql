CREATE TABLE patients (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    mrn VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    gender VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    emergency_contact_name VARCHAR(150),
    emergency_contact_phone VARCHAR(20),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_patients_tenant_mrn UNIQUE (tenant_id, mrn),
    CONSTRAINT fk_patients_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_patients_tenant_id ON patients(tenant_id);
CREATE INDEX idx_patients_tenant_phone ON patients(tenant_id, phone);
CREATE INDEX idx_patients_tenant_name ON patients(tenant_id, LOWER(first_name), LOWER(last_name));

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    appointment_number VARCHAR(50) NOT NULL,
    appointment_type VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason_for_visit VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_appointments_tenant_number UNIQUE (tenant_id, appointment_number),
    CONSTRAINT fk_appointments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_appointments_tenant_doctor_time ON appointments(tenant_id, doctor_id, start_time, end_time);
CREATE INDEX idx_appointments_tenant_branch_time ON appointments(tenant_id, branch_id, start_time, end_time);
CREATE INDEX idx_appointments_tenant_patient ON appointments(tenant_id, patient_id);

CREATE TABLE visits (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    appointment_id UUID,
    visit_number VARCHAR(50) NOT NULL,
    visit_date TIMESTAMP NOT NULL,
    chief_complaint VARCHAR(1000),
    diagnosis VARCHAR(1000),
    notes VARCHAR(2000),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_visits_tenant_number UNIQUE (tenant_id, visit_number),
    CONSTRAINT fk_visits_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_visits_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT fk_visits_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_visits_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_visits_doctor FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_visits_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL
);

CREATE INDEX idx_visits_tenant_patient ON visits(tenant_id, patient_id);
CREATE INDEX idx_visits_tenant_doctor ON visits(tenant_id, doctor_id);
