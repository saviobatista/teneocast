-- Create player_service table (since 'player' already exists)
CREATE TABLE player_service (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    pairing_code VARCHAR(10),
    pairing_code_expiry TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'OFFLINE',
    platform VARCHAR(50) NOT NULL,
    device_info TEXT,
    app_version VARCHAR(100),
    last_seen TIMESTAMP,
    current_track VARCHAR(255),
    volume INTEGER DEFAULT 50,
    is_online BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_player_service_tenant_id ON player_service(tenant_id);
CREATE INDEX idx_player_service_pairing_code ON player_service(pairing_code);
CREATE INDEX idx_player_service_status ON player_service(status);
CREATE INDEX idx_player_service_online ON player_service(is_online);
CREATE INDEX idx_player_service_last_seen ON player_service(last_seen);
CREATE INDEX idx_player_service_pairing_expiry ON player_service(pairing_code_expiry);

-- Create unique constraint for pairing code
CREATE UNIQUE INDEX idx_player_service_pairing_code_unique ON player_service(pairing_code) WHERE pairing_code IS NOT NULL;