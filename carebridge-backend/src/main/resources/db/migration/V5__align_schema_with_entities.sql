ALTER TABLE branches
    ALTER COLUMN email TYPE VARCHAR(150);

ALTER TABLE branches
    ALTER COLUMN email DROP NOT NULL;

ALTER TABLE clinics
    ADD CONSTRAINT uq_clinics_clinic_code UNIQUE (clinic_code);

CREATE UNIQUE INDEX uq_tenants_email_lower
    ON tenants (LOWER(email));
