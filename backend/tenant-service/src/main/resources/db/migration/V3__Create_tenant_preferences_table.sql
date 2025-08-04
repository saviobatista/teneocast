-- Create tenant_preferences table
CREATE TABLE tenant_preferences (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL UNIQUE,
    playback_settings JSONB NOT NULL DEFAULT '{}',
    genre_preferences JSONB NOT NULL DEFAULT '[]',
    ad_rules JSONB NOT NULL DEFAULT '{}',
    volume_default INTEGER NOT NULL DEFAULT 50,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_tenant_preferences_tenant_id 
        FOREIGN KEY (tenant_id) 
        REFERENCES tenants(id) 
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_tenant_preferences_tenant_id ON tenant_preferences(tenant_id);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_tenant_preferences_updated_at 
    BEFORE UPDATE ON tenant_preferences 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column(); 