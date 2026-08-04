CREATE TABLE branches(
    id UUID PRIMARY KEY,
     tenant_id UUID NOT NULL,
     clinic_id UUID NOT NULL,
     branch_code VARCHAR(50) NOT NULL,
     branch_name VARCHAR(150) NOT NULL,
     email VARCHAR(250) NOT NULL,
     phone VARCHAR(20),
     address_line1 VARCHAR(255),

    address_line2 VARCHAR(255),

    city VARCHAR(100),

    state VARCHAR(100),

    postal_code VARCHAR(20),

    country VARCHAR(100),

    is_main_branch BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,


    CONSTRAINT uq_branches_tenant_code
        UNIQUE (tenant_id, branch_code),

    CONSTRAINT fk_branches_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_branches_clinic
        FOREIGN KEY (clinic_id)
        REFERENCES clinics(id)
        ON DELETE CASCADE

);
CREATE INDEX idx_branches_tenant_id
    ON branches(tenant_id);

CREATE INDEX idx_branches_clinic_id
    ON branches(clinic_id);

CREATE UNIQUE INDEX uq_branches_main_branch_per_clinic
    ON branches(clinic_id)
    WHERE is_main_branch = TRUE;