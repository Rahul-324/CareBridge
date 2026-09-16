CREATE TABLE lab_test_catalog (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    test_code VARCHAR(50) NOT NULL,
    test_name VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    description VARCHAR(500),
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    reference_range VARCHAR(255),
    turnaround_time VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_lab_test_catalog_tenant_code UNIQUE (tenant_id, test_code),
    CONSTRAINT fk_lab_test_catalog_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_lab_catalog_tenant_id ON lab_test_catalog(tenant_id);

CREATE TABLE lab_orders (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    visit_id UUID,
    doctor_id UUID NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_lab_orders_tenant_number UNIQUE (tenant_id, order_number),
    CONSTRAINT fk_lab_orders_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_orders_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_orders_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_orders_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_orders_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE SET NULL,
    CONSTRAINT fk_lab_orders_doctor FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_lab_orders_tenant_patient ON lab_orders(tenant_id, patient_id);
CREATE INDEX idx_lab_orders_tenant_visit ON lab_orders(tenant_id, visit_id);

CREATE TABLE lab_order_items (
    id UUID PRIMARY KEY,
    lab_order_id UUID NOT NULL,
    test_catalog_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    result_text VARCHAR(2000),
    reference_range VARCHAR(255),
    unit VARCHAR(50),
    file_url VARCHAR(500),
    performed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_lab_order_items_order FOREIGN KEY (lab_order_id) REFERENCES lab_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_order_items_catalog FOREIGN KEY (test_catalog_id) REFERENCES lab_test_catalog(id) ON DELETE CASCADE
);

CREATE INDEX idx_lab_order_items_order_id ON lab_order_items(lab_order_id);

CREATE TABLE lab_samples (
    id UUID PRIMARY KEY,
    lab_order_item_id UUID NOT NULL,
    sample_number VARCHAR(50) NOT NULL,
    sample_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    collected_at TIMESTAMP,
    received_at TIMESTAMP,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_lab_samples_order_item FOREIGN KEY (lab_order_item_id) REFERENCES lab_order_items(id) ON DELETE CASCADE
);

CREATE INDEX idx_lab_samples_order_item_id ON lab_samples(lab_order_item_id);
