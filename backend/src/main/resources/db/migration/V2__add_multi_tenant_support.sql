-- Add multi-tenant support
-- Migration V2: Add tenant_id to users table and create performance indices

-- Step 1: Add tenant_id column to users table (nullable)
-- Will be set automatically by @PostPersist hook for new users
ALTER TABLE users ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Step 2: Set tenant_id to user's own ID for existing users (self-tenant model)
UPDATE users SET tenant_id = id WHERE tenant_id IS NULL;

-- Step 3: Note: tenant_id remains nullable to allow @PostPersist to set it
-- For new users, @PostPersist sets tenant_id = id after insert
-- For existing users, migration sets tenant_id = id above

-- Step 4: Create index on tenant_id for fast tenant-based queries
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);

-- Step 5: Create composite index on invoices for tenant-based queries
-- This assumes invoices reference users via user_id
CREATE INDEX IF NOT EXISTS idx_invoices_user_tenant ON invoices(user_id);

-- Step 6: Create index for invoice status + date queries (for overdue job)
CREATE INDEX IF NOT EXISTS idx_invoices_status_due_date ON invoices(status, due_date);

-- Step 7: Create index for invoice created_at (for stats aggregation)
CREATE INDEX IF NOT EXISTS idx_invoices_created_at ON invoices(created_at);

-- Step 8: Create index for reminders scheduled_at (for reminder job)
CREATE INDEX IF NOT EXISTS idx_reminders_scheduled_sent ON reminders(scheduled_at, sent);

-- Step 9: Create index for OTP cleanup (for OTP cleanup job)
CREATE INDEX IF NOT EXISTS idx_users_otp_expiry ON users(otp_expiry) WHERE otp_secret IS NOT NULL;

-- Step 10: Add comments for documentation
COMMENT ON COLUMN users.tenant_id IS 'Tenant ID for multi-tenancy. Default: user own ID (self-tenant). Future: shared tenant for teams/cabinets.';
COMMENT ON INDEX idx_users_tenant_id IS 'Performance index for tenant-based user queries';
COMMENT ON INDEX idx_invoices_user_tenant IS 'Performance index for tenant-based invoice queries';
COMMENT ON INDEX idx_invoices_status_due_date IS 'Performance index for overdue invoice detection';
COMMENT ON INDEX idx_invoices_created_at IS 'Performance index for stats aggregation by date range';
COMMENT ON INDEX idx_reminders_scheduled_sent IS 'Performance index for pending reminder queries';
COMMENT ON INDEX idx_users_otp_expiry IS 'Performance index for OTP cleanup job (partial index)';
