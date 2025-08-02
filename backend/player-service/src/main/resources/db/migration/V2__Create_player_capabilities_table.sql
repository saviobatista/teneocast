-- Create player capabilities table
CREATE TABLE player_service_capabilities (
    player_id VARCHAR(36) NOT NULL,
    capability VARCHAR(50) NOT NULL,
    PRIMARY KEY (player_id, capability),
    FOREIGN KEY (player_id) REFERENCES player_service(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_player_service_capabilities_player_id ON player_service_capabilities(player_id);