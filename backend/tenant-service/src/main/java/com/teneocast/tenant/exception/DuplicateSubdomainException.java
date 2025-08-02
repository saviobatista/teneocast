package com.teneocast.tenant.exception;

public class DuplicateSubdomainException extends RuntimeException {
    
    public DuplicateSubdomainException(String message) {
        super(message);
    }
    
    public DuplicateSubdomainException(String message, Throwable cause) {
        super(message, cause);
    }
} 