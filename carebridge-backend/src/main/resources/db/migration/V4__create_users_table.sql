CREATE TABLE users (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    branch_id UUID,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100),

    email VARCHAR(150) NOT NULL,

    phone VARCHAR(20),

    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(40) NOT NULL,

    status VARCHAR(30) NOT NULL,

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_users_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(id)
        ON DELETE SET NULL
);
CREATE UNIQUE INDEX uq_users_email_lower
    ON users (LOWER(email));

CREATE INDEX idx_users_tenant_id
    ON users (tenant_id);

CREATE INDEX idx_users_branch_id
    ON users (branch_id);