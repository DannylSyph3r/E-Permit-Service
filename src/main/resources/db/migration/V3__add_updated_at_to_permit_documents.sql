-- PermitDocument extends Auditable which maps updatedAt.
-- The original V1 migration omitted this column. Adding it here so Hibernate
-- schema validation (ddl-auto=validate) passes on startup.
ALTER TABLE permit_documents ADD COLUMN updated_at TIMESTAMP;