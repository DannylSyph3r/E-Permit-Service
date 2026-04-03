-- User entity declares an 'enabled' field (UserDetails contract).
-- The original V1 migration omitted this column.
-- Adding it here with a default of true so existing seeded rows remain valid.
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;