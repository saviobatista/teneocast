package com.teneocast.tenant.exception;

public class TenantValidationException extends RuntimeException {
    
    public TenantValidationException(String message) {
        super(message);
    }
    
    public TenantValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 