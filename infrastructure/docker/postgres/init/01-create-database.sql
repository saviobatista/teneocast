-- TeneoCast Database Schema
-- This script creates the initial database structure

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ================================
-- MUSIC GENDER TABLE
-- ================================
CREATE TABLE music_gender (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- MUSIC TABLE
-- ================================
CREATE TABLE music (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(500) NOT NULL,
    gender_id UUID NOT NULL REFERENCES music_gender(id) ON DELETE CASCADE,
    file VARCHAR(1000) NOT NULL, -- S3 object key
    metadata JSONB,
    duration_seconds INTEGER,
    file_size_bytes BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- TENANT TABLE
-- ================================
CREATE TABLE tenant (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(100) NOT NULL UNIQUE,
    preferences JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- AD TYPE TABLE
-- ================================
CREATE TABLE ad_type (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID REFERENCES tenant(id) ON DELETE CASCADE, -- nullable: null = default/global
    name VARCHAR(255) NOT NULL,
    is_selectable BOOLEAN DEFAULT TRUE,
    can_play_remotely BOOLEAN DEFAULT TRUE,
    can_play_individually BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- AD RULE TABLE
-- ================================
CREATE TABLE ad_rule (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    ad_type_id UUID NOT NULL REFERENCES ad_type(id) ON DELETE CASCADE,
    music_interval INTEGER NOT NULL DEFAULT 5, -- # of musics before ad sequence
    ads_per_interval INTEGER NOT NULL DEFAULT 1, -- # of ads played per sequence
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(tenant_id, ad_type_id)
);

-- ================================
-- AD MEDIA TABLE
-- ================================
CREATE TABLE ad_media (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    ad_type_id UUID NOT NULL REFERENCES ad_type(id) ON DELETE CASCADE,
    name VARCHAR(500) NOT NULL,
    file VARCHAR(1000) NOT NULL, -- S3 object key
    metadata JSONB,
    duration_seconds INTEGER,
    file_size_bytes BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- USER TABLE
-- ================================
CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID REFERENCES tenant(id) ON DELETE CASCADE, -- nullable for ROOT/OPERATOR
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ROOT', 'OPERATOR', 'MASTER', 'PRODUCER', 'MANAGER', 'PLAYER')),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- ================================
-- PLAYER TABLE
-- ================================
CREATE TABLE player (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    pairing_code VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    settings JSONB,
    platform VARCHAR(50), -- 'web', 'windows', 'android'
    device_info JSONB,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(tenant_id, name)
);

-- ================================
-- PAIRING CODES TABLE (for Redis-like functionality in DB)
-- ================================
CREATE TABLE pairing_code (
    code VARCHAR(20) PRIMARY KEY,
    player_id UUID REFERENCES player(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- PLAYBACK LOG TABLE (for analytics)
-- ================================
CREATE TABLE playback_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    player_id UUID NOT NULL REFERENCES player(id) ON DELETE CASCADE,
    content_type VARCHAR(50) NOT NULL CHECK (content_type IN ('music', 'ad', 'tts')),
    content_id UUID, -- references music.id, ad_media.id, or null for TTS
    content_name VARCHAR(500),
    action VARCHAR(50) NOT NULL CHECK (action IN ('play', 'pause', 'skip', 'stop', 'complete')),
    timestamp_utc TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

-- ================================
-- INDEXES
-- ================================

-- User indexes
CREATE INDEX idx_user_tenant_id ON "user"(tenant_id);
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_role ON "user"(role);

-- Player indexes
CREATE INDEX idx_player_tenant_id ON player(tenant_id);
CREATE INDEX idx_player_pairing_code ON player(pairing_code) WHERE pairing_code IS NOT NULL;
CREATE INDEX idx_player_active ON player(is_active) WHERE is_active = TRUE;

-- Music indexes
CREATE INDEX idx_music_gender_id ON music(gender_id);
CREATE INDEX idx_music_name_trgm ON music USING gin(name gin_trgm_ops);

-- Ad indexes
CREATE INDEX idx_ad_media_tenant_id ON ad_media(tenant_id);
CREATE INDEX idx_ad_media_type_id ON ad_media(ad_type_id);
CREATE INDEX idx_ad_rule_tenant_id ON ad_rule(tenant_id);

-- Playback log indexes
CREATE INDEX idx_playback_log_player_id ON playback_log(player_id);
CREATE INDEX idx_playback_log_timestamp ON playback_log(timestamp_utc);
CREATE INDEX idx_playback_log_content_type ON playback_log(content_type);

-- Pairing code cleanup index
CREATE INDEX idx_pairing_code_expires_at ON pairing_code(expires_at);

-- ================================
-- INITIAL DATA
-- ================================

-- Insert default music genders
INSERT INTO music_gender (name) VALUES 
    ('Pop'),
    ('Rock'),
    ('Jazz'),
    ('Classical'),
    ('Electronic'),
    ('Hip Hop'),
    ('Country'),
    ('R&B'),
    ('Alternative'),
    ('Ambient');

-- Insert default ad types (global)
INSERT INTO ad_type (tenant_id, name, is_selectable, can_play_remotely, can_play_individually) VALUES
    (NULL, 'General Advertisement', TRUE, TRUE, TRUE),
    (NULL, 'Promotional Announcement', TRUE, TRUE, TRUE),
    (NULL, 'Safety Message', FALSE, TRUE, FALSE),
    (NULL, 'Store Information', TRUE, TRUE, TRUE);

-- ================================
-- FUNCTIONS AND TRIGGERS
-- ================================

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers to all tables with updated_at
CREATE TRIGGER update_music_gender_updated_at BEFORE UPDATE ON music_gender FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_music_updated_at BEFORE UPDATE ON music FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tenant_updated_at BEFORE UPDATE ON tenant FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ad_type_updated_at BEFORE UPDATE ON ad_type FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ad_rule_updated_at BEFORE UPDATE ON ad_rule FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ad_media_updated_at BEFORE UPDATE ON ad_media FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_updated_at BEFORE UPDATE ON "user" FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_player_updated_at BEFORE UPDATE ON player FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Cleanup expired pairing codes function
CREATE OR REPLACE FUNCTION cleanup_expired_pairing_codes()
RETURNS void AS $$
BEGIN
    DELETE FROM pairing_code WHERE expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql; 