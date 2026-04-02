-- Seed one test user per tenant.
-- Passwords are hashed at migration time using pgcrypto bcrypt (cost 10).
-- Credentials: health@test.com / test1234 and education@test.com / test1234
INSERT INTO users (email, password, tenant_id)
VALUES
    ('health@test.com',    crypt('test1234', gen_salt('bf', 10)), 'Ministry_Health'),
    ('education@test.com', crypt('test1234', gen_salt('bf', 10)), 'Ministry_Education');