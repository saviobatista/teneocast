-- V1__Create_admin_schema.sql
-- Create admin service database schema

-- Create admin_users table
CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ROOT', 'OPERATOR')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Create platform_settings table
CREATE TABLE IF NOT EXISTS platform_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(255) UNIQUE NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(50) NOT NULL CHECK (setting_type IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON', 'DECIMAL')),
    description TEXT,
    updated_by UUID REFERENCES admin_users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create impersonation_sessions table
CREATE TABLE IF NOT EXISTS impersonation_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_user_id UUID NOT NULL REFERENCES admin_users(id),
    target_user_id UUID NOT NULL,
    target_tenant_id UUID NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB,
    ended_at TIMESTAMP,
    end_reason VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users(email);
CREATE INDEX IF NOT EXISTS idx_admin_users_role ON admin_users(role);
CREATE INDEX IF NOT EXISTS idx_admin_users_active ON admin_users(is_active);

CREATE INDEX IF NOT EXISTS idx_platform_settings_key ON platform_settings(setting_key);
CREATE INDEX IF NOT EXISTS idx_platform_settings_type ON platform_settings(setting_type);

CREATE INDEX IF NOT EXISTS idx_impersonation_sessions_admin ON impersonation_sessions(admin_user_id);
CREATE INDEX IF NOT EXISTS idx_impersonation_sessions_target ON impersonation_sessions(target_user_id);
CREATE INDEX IF NOT EXISTS idx_impersonation_sessions_tenant ON impersonation_sessions(target_tenant_id);
CREATE INDEX IF NOT EXISTS idx_impersonation_sessions_active ON impersonation_sessions(is_active);
CREATE INDEX IF NOT EXISTS idx_impersonation_sessions_expires ON impersonation_sessions(expires_at);

-- Insert default admin user (password: admin123 - should be changed in production)
INSERT INTO admin_users (email, password_hash, role, is_active) 
VALUES ('admin@teneocast.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ROOT', true)
ON CONFLICT (email) DO NOTHING;

-- Insert default platform settings
INSERT INTO platform_settings (setting_key, setting_value, setting_type, description) VALUES
('platform.name', 'TeneoCast', 'STRING', 'Platform name'),
('platform.version', '1.0.0', 'STRING', 'Platform version'),
('platform.maintenance_mode', 'false', 'BOOLEAN', 'Platform maintenance mode'),
('impersonation.session_timeout_minutes', '60', 'INTEGER', 'Impersonation session timeout in minutes'),
('impersonation.max_concurrent_sessions', '5', 'INTEGER', 'Maximum concurrent impersonation sessions per admin'),
('security.jwt.expiration_minutes', '15', 'INTEGER', 'JWT token expiration time in minutes')
ON CONFLICT (setting_key) DO NOTHING;
