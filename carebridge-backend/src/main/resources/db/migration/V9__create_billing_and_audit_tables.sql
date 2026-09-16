CREATE TABLE price_list_items (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_price_list_tenant_code UNIQUE (tenant_id, item_code),
    CONSTRAINT fk_price_list_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_price_list_tenant_id ON price_list_items(tenant_id);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    visit_id UUID,
    lab_order_id UUID,
    invoice_number VARCHAR(50) NOT NULL,
    invoice_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP,
    subtotal NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    tax_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    paid_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_invoices_tenant_number UNIQUE (tenant_id, invoice_number),
    CONSTRAINT fk_invoices_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE SET NULL,
    CONSTRAINT fk_invoices_lab_order FOREIGN KEY (lab_order_id) REFERENCES lab_orders(id) ON DELETE SET NULL
);

CREATE INDEX idx_invoices_tenant_patient ON invoices(tenant_id, patient_id);
CREATE INDEX idx_invoices_tenant_visit ON invoices(tenant_id, visit_id);
CREATE INDEX idx_invoices_tenant_lab_order ON invoices(tenant_id, lab_order_id);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    quantity INT NOT NULL DEFAULT 1,
    total_price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE INDEX idx_invoice_items_invoice_id ON invoice_items(invoice_id);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    payment_number VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(50) NOT NULL,
    transaction_reference VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_payments_tenant_number UNIQUE (tenant_id, payment_number),
    CONSTRAINT fk_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_tenant_invoice ON payments(tenant_id, invoice_id);

CREATE TABLE phi_audit_logs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID,
    user_email VARCHAR(150),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID,
    ip_address VARCHAR(45),
    details VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL,

    CONSTRAINT fk_phi_audit_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_phi_audit_tenant_time ON phi_audit_logs(tenant_id, timestamp);
