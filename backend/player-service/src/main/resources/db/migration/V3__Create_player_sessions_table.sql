-- Create player sessions table
CREATE TABLE player_service_sessions (
    id VARCHAR(36) PRIMARY KEY,
    player_id VARCHAR(36) NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    connected_at TIMESTAMP,
    disconnected_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_ping_at TIMESTAMP,
    connection_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (player_id) REFERENCES player_service(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_player_service_sessions_player_id ON player_service_sessions(player_id);
CREATE INDEX idx_player_service_sessions_session_id ON player_service_sessions(session_id);
CREATE INDEX idx_player_service_sessions_active ON player_service_sessions(is_active);
CREATE INDEX idx_player_service_sessions_connected_at ON player_service_sessions(connected_at);
CREATE INDEX idx_player_service_sessions_last_ping ON player_service_sessions(last_ping_at);
CREATE INDEX idx_player_service_sessions_player_active ON player_service_sessions(player_id, is_active);