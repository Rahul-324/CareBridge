CREATE TABLE clinics(
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    clinic_code VARCHAR(50) NOT NULL,
    clinic_name VARCHAR(150) NOT NULL,
    clinic_type VARCHAR(40) NOT NULL,
    registration_number VARCHAR(100),
    email VARCHAR(150),
    phone VARCHAR(20),
    website VARCHAR(255),
    logo_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_clinics_tenant
        UNIQUE (tenant_id),

    CONSTRAINT fk_clinics_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE
);