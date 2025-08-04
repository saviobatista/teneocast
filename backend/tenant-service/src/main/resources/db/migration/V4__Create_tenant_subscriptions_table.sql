-- Create tenant_subscriptions table
CREATE TABLE tenant_subscriptions (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL UNIQUE,
    plan_type VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    plan_name VARCHAR(100) NOT NULL,
    max_users INTEGER NOT NULL DEFAULT 10,
    max_storage_gb INTEGER NOT NULL DEFAULT 5,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    billing_cycle VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    next_billing_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_tenant_subscriptions_tenant_id 
        FOREIGN KEY (tenant_id) 
        REFERENCES tenants(id) 
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_tenant_subscriptions_tenant_id ON tenant_subscriptions(tenant_id);
CREATE INDEX idx_tenant_subscriptions_plan_type ON tenant_subscriptions(plan_type);
CREATE INDEX idx_tenant_subscriptions_active ON tenant_subscriptions(is_active);
CREATE INDEX idx_tenant_subscriptions_next_billing ON tenant_subscriptions(next_billing_date);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_tenant_subscriptions_updated_at 
    BEFORE UPDATE ON tenant_subscriptions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column(); 