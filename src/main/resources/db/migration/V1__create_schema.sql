-- pgcrypto is needed for V2 seed data hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create the application runtime user (limited permissions, subject to RLS)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'epermit_app') THEN
CREATE ROLE epermit_app WITH LOGIN PASSWORD 'nfhrBJsFfx2r+jGbL8jDNYoP8+euohsvmlc2lgGFedo=';
END IF;
END
$$;

-- Users table (no RLS — authentication layer, not tenant-scoped)
CREATE TABLE users (
                       id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       email       VARCHAR(255) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       tenant_id   VARCHAR(100) NOT NULL,
                       created_at  TIMESTAMP    NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMP
);

-- Permits table
CREATE TABLE permits (
                         id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         tenant_id       VARCHAR(100) NOT NULL,
                         applicant_name  VARCHAR(255) NOT NULL,
                         applicant_email VARCHAR(255) NOT NULL,
                         permit_type     VARCHAR(50)  NOT NULL,
                         amount          BIGINT       NOT NULL,
                         permit_status   VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                         payment_status  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                         created_at      TIMESTAMP    NOT NULL DEFAULT now(),
                         updated_at      TIMESTAMP
);

-- Permit documents table (child of permits)
CREATE TABLE permit_documents (
                                  id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                  permit_id     UUID         NOT NULL REFERENCES permits(id) ON DELETE CASCADE,
                                  document_type VARCHAR(100) NOT NULL,
                                  document_url  VARCHAR(500) NOT NULL,
                                  created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Grant runtime user access
GRANT SELECT, INSERT, UPDATE, DELETE ON users TO epermit_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON permits TO epermit_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON permit_documents TO epermit_app;

-- Enable RLS on permits
-- FORCE ensures even the table owner is subject to the policy
ALTER TABLE permits ENABLE ROW LEVEL SECURITY;
ALTER TABLE permits FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON permits
    USING (tenant_id = current_setting('app.current_tenant', true));

-- Enable RLS on permit_documents
-- Policy joins via permits so tenant isolation propagates to child rows
ALTER TABLE permit_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE permit_documents FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON permit_documents
    USING (
        permit_id IN (
            SELECT id FROM permits
            WHERE tenant_id = current_setting('app.current_tenant', true)
        )
    );