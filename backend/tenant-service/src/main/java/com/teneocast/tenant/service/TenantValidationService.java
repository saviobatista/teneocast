package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.CreateTenantRequest;
import com.teneocast.tenant.dto.UpdateTenantRequest;
import com.teneocast.tenant.exception.TenantValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
@Slf4j
public class TenantValidationService {

    private static final Pattern SUBDOMAIN_PATTERN = Pattern.compile("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_SUBDOMAIN_LENGTH = 3;
    private static final int MAX_SUBDOMAIN_LENGTH = 63;

    /**
     * Validate create tenant request
     */
    public void validateCreateTenantRequest(CreateTenantRequest request) {
        log.debug("Validating create tenant request: {}", request);
        
        if (request == null) {
            throw new TenantValidationException("Create tenant request cannot be null");
        }
        
        validateTenantName(request.getName());
        validateSubdomain(request.getSubdomain());
    }

    /**
     * Validate update tenant request
     */
    public void validateUpdateTenantRequest(UpdateTenantRequest request) {
        log.debug("Validating update tenant request: {}", request);
        
        if (request == null) {
            throw new TenantValidationException("Update tenant request cannot be null");
        }
        
        if (request.getName() != null) {
            validateTenantName(request.getName());
        }
        
        if (request.getSubdomain() != null) {
            validateSubdomain(request.getSubdomain());
        }
    }

    /**
     * Validate tenant name
     */
    public void validateTenantName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new TenantValidationException("Tenant name cannot be null or empty");
        }
        
        if (name.length() < MIN_NAME_LENGTH) {
            throw new TenantValidationException("Tenant name must be at least " + MIN_NAME_LENGTH + " characters long");
        }
        
        if (name.length() > MAX_NAME_LENGTH) {
            throw new TenantValidationException("Tenant name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        
        if (!name.matches("^[a-zA-Z0-9\\s\\-_]+$")) {
            throw new TenantValidationException("Tenant name contains invalid characters. Only letters, numbers, spaces, hyphens, and underscores are allowed");
        }
    }

    /**
     * Validate subdomain
     */
    public void validateSubdomain(String subdomain) {
        if (!StringUtils.hasText(subdomain)) {
            throw new TenantValidationException("Subdomain cannot be null or empty");
        }
        
        if (subdomain.length() < MIN_SUBDOMAIN_LENGTH) {
            throw new TenantValidationException("Subdomain must be at least " + MIN_SUBDOMAIN_LENGTH + " characters long");
        }
        
        if (subdomain.length() > MAX_SUBDOMAIN_LENGTH) {
            throw new TenantValidationException("Subdomain cannot exceed " + MAX_SUBDOMAIN_LENGTH + " characters");
        }
        
        if (!SUBDOMAIN_PATTERN.matcher(subdomain).matches()) {
            throw new TenantValidationException("Subdomain contains invalid characters. Only lowercase letters, numbers, and hyphens are allowed. Cannot start or end with hyphen");
        }
        
        // Check for reserved subdomains
        if (isReservedSubdomain(subdomain)) {
            throw new TenantValidationException("Subdomain '" + subdomain + "' is reserved and cannot be used");
        }
    }

    /**
     * Check if subdomain is reserved
     */
    private boolean isReservedSubdomain(String subdomain) {
        String[] reservedSubdomains = {
            "www", "api", "admin", "app", "mail", "ftp", "smtp", "pop", "imap",
            "ns1", "ns2", "dns", "web", "blog", "shop", "store", "help", "support",
            "status", "cdn", "static", "assets", "images", "files", "docs", "test",
            "dev", "staging", "beta", "alpha", "demo", "example", "localhost"
        };
        
        for (String reserved : reservedSubdomains) {
            if (reserved.equalsIgnoreCase(subdomain)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Validate tenant ID format
     */
    public void validateTenantId(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            throw new TenantValidationException("Tenant ID cannot be null or empty");
        }
        
        if (!tenantId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            throw new TenantValidationException("Invalid tenant ID format. Expected UUID format");
        }
    }

    /**
     * Validate pagination parameters
     */
    public void validatePagination(int page, int size) {
        if (page < 0) {
            throw new TenantValidationException("Page number cannot be negative");
        }
        
        if (size < 1) {
            throw new TenantValidationException("Page size must be at least 1");
        }
        
        if (size > 100) {
            throw new TenantValidationException("Page size cannot exceed 100");
        }
    }

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
} 