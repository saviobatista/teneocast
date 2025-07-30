-- Create auth schema for auth-service
CREATE SCHEMA IF NOT EXISTS auth;

-- Create users table in auth schema
CREATE TABLE auth.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- Create refresh tokens table in auth schema
CREATE TABLE auth.refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT false
);

-- Create email verification tokens table in auth schema
CREATE TABLE auth.email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create password reset tokens table in auth schema
CREATE TABLE auth.password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_auth_users_username ON auth.users(username);
CREATE INDEX idx_auth_users_email ON auth.users(email);
CREATE INDEX idx_auth_users_role ON auth.users(role);
CREATE INDEX idx_auth_refresh_tokens_user_id ON auth.refresh_tokens(user_id);
CREATE INDEX idx_auth_refresh_tokens_token ON auth.refresh_tokens(token);
CREATE INDEX idx_auth_refresh_tokens_expires_at ON auth.refresh_tokens(expires_at);
CREATE INDEX idx_auth_email_verification_tokens_user_id ON auth.email_verification_tokens(user_id);
CREATE INDEX idx_auth_email_verification_tokens_token ON auth.email_verification_tokens(token);
CREATE INDEX idx_auth_password_reset_tokens_user_id ON auth.password_reset_tokens(user_id);
CREATE INDEX idx_auth_password_reset_tokens_token ON auth.password_reset_tokens(token);

-- Insert default admin user (password: admin123)
INSERT INTO auth.users (username, email, password_hash, first_name, last_name, role, is_active, is_email_verified)
VALUES (
    'admin',
    'admin@teneocast.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- admin123
    'Admin',
    'User',
    'ADMIN',
    true,
    true
); 