-- ============================================================
-- Finance App Schema
-- PostgreSQL 15+
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- ENUM Types
-- ============================================================
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('ADMIN', 'ANALYST', 'VIEWER');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE record_type AS ENUM ('INCOME', 'EXPENSE');
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- ============================================================
-- Users Table
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    role        user_role    NOT NULL DEFAULT 'VIEWER',
    status      user_status  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role     ON users(role);

-- ============================================================
-- Financial Records Table
-- ============================================================
CREATE TABLE IF NOT EXISTS financial_records (
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    type        record_type   NOT NULL,
    category    VARCHAR(100)  NOT NULL,
    record_date DATE          NOT NULL,
    description TEXT,
    created_by  BIGINT        NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_records_type        ON financial_records(type);
CREATE INDEX IF NOT EXISTS idx_records_category    ON financial_records(category);
CREATE INDEX IF NOT EXISTS idx_records_date        ON financial_records(record_date);
CREATE INDEX IF NOT EXISTS idx_records_created_by  ON financial_records(created_by);

-- Composite index for common dashboard queries
CREATE INDEX IF NOT EXISTS idx_records_date_type   ON financial_records(record_date, type);

-- ============================================================
-- Auto-update updated_at trigger function
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER trg_records_updated_at
    BEFORE UPDATE ON financial_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- Seed Data — default users (passwords are BCrypt of 'password123')
-- ============================================================
INSERT INTO users (username, password, email, role, status) VALUES
    ('admin',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/H5MbBVi', 'admin@financeapp.com',   'ADMIN',   'ACTIVE'),
    ('analyst', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/H5MbBVi', 'analyst@financeapp.com', 'ANALYST', 'ACTIVE'),
    ('viewer',  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/H5MbBVi', 'viewer@financeapp.com',  'VIEWER',  'ACTIVE')
ON CONFLICT (username) DO NOTHING;

-- ============================================================
-- Seed Data — sample financial records (linked to admin user id=1)
-- ============================================================
INSERT INTO financial_records (amount, type, category, record_date, description, created_by) VALUES
    (85000.00, 'INCOME',  'Salary',        CURRENT_DATE - INTERVAL '1 month',  'Monthly salary',               1),
    (85000.00, 'INCOME',  'Salary',        CURRENT_DATE - INTERVAL '2 months', 'Monthly salary',               1),
    (85000.00, 'INCOME',  'Salary',        CURRENT_DATE - INTERVAL '3 months', 'Monthly salary',               1),
    (85000.00, 'INCOME',  'Salary',        CURRENT_DATE - INTERVAL '4 months', 'Monthly salary',               1),
    (85000.00, 'INCOME',  'Salary',        CURRENT_DATE - INTERVAL '5 months', 'Monthly salary',               1),
    (85000.00, 'INCOME',  'Salary',        CURRENT_DATE,                        'Monthly salary',               1),
    (12000.00, 'INCOME',  'Freelance',     CURRENT_DATE - INTERVAL '1 month',  'Freelance project payment',    1),
    ( 5000.00, 'INCOME',  'Investments',   CURRENT_DATE - INTERVAL '2 months', 'Dividend income',              1),
    (18000.00, 'EXPENSE', 'Rent',          CURRENT_DATE - INTERVAL '1 month',  'Monthly apartment rent',       1),
    (18000.00, 'EXPENSE', 'Rent',          CURRENT_DATE - INTERVAL '2 months', 'Monthly apartment rent',       1),
    (18000.00, 'EXPENSE', 'Rent',          CURRENT_DATE - INTERVAL '3 months', 'Monthly apartment rent',       1),
    (18000.00, 'EXPENSE', 'Rent',          CURRENT_DATE - INTERVAL '4 months', 'Monthly apartment rent',       1),
    ( 8500.00, 'EXPENSE', 'Food',          CURRENT_DATE - INTERVAL '1 month',  'Groceries and dining',         1),
    ( 7200.00, 'EXPENSE', 'Food',          CURRENT_DATE - INTERVAL '2 months', 'Groceries and dining',         1),
    ( 3200.00, 'EXPENSE', 'Transport',     CURRENT_DATE - INTERVAL '1 month',  'Fuel and public transport',    1),
    ( 2800.00, 'EXPENSE', 'Transport',     CURRENT_DATE - INTERVAL '2 months', 'Fuel and public transport',    1),
    (15000.00, 'EXPENSE', 'Healthcare',    CURRENT_DATE - INTERVAL '3 months', 'Annual health checkup',        1),
    ( 4500.00, 'EXPENSE', 'Entertainment', CURRENT_DATE - INTERVAL '1 month',  'Movies and subscriptions',     1),
    ( 6000.00, 'EXPENSE', 'Utilities',     CURRENT_DATE - INTERVAL '1 month',  'Electricity and internet',     1),
    ( 5800.00, 'EXPENSE', 'Utilities',     CURRENT_DATE - INTERVAL '2 months', 'Electricity and internet',     1),
    ( 9000.00, 'EXPENSE', 'Education',     CURRENT_DATE - INTERVAL '4 months', 'Online courses',               1),
    ( 3000.00, 'EXPENSE', 'Clothing',      CURRENT_DATE - INTERVAL '2 months', 'Seasonal wardrobe update',     1),
    ( 1200.00, 'EXPENSE', 'Entertainment', CURRENT_DATE - INTERVAL '3 months', 'Concert tickets',              1)
ON CONFLICT DO NOTHING;
