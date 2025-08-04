package com.teneocast.tenant.controller;

import com.teneocast.tenant.dto.CreateTenantRequest;
import com.teneocast.tenant.dto.TenantDto;
import com.teneocast.tenant.dto.UpdateTenantRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.exception.DuplicateSubdomainException;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantService;
import com.teneocast.tenant.service.TenantValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;
    private final TenantValidationService tenantValidationService;

    /**
     * Create a new tenant
     */
    @PostMapping
    public ResponseEntity<TenantDto> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        log.info("Creating tenant with subdomain: {}", request.getSubdomain());
        
        try {
            TenantDto tenant = tenantService.createTenant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
        } catch (DuplicateSubdomainException e) {
            log.warn("Duplicate subdomain attempt: {}", request.getSubdomain());
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error creating tenant: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get tenant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TenantDto> getTenantById(@PathVariable String id) {
        log.debug("Getting tenant by ID: {}", id);
        
        try {
            tenantValidationService.validateTenantId(id);
            TenantDto tenant = tenantService.getTenantById(id);
            return ResponseEntity.ok(tenant);
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found with ID: {}", id);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", id);
            throw e;
        }
    }

    /**
     * Get tenant by subdomain
     */
    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<TenantDto> getTenantBySubdomain(@PathVariable String subdomain) {
        log.debug("Getting tenant by subdomain: {}", subdomain);
        
        try {
            TenantDto tenant = tenantService.getTenantBySubdomain(subdomain);
            return ResponseEntity.ok(tenant);
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found with subdomain: {}", subdomain);
            throw e;
        }
    }

    /**
     * Update tenant
     */
    @PutMapping("/{id}")
    public ResponseEntity<TenantDto> updateTenant(@PathVariable String id, 
                                                @Valid @RequestBody UpdateTenantRequest request) {
        log.info("Updating tenant with ID: {}", id);
        
        try {
            tenantValidationService.validateTenantId(id);
            TenantDto tenant = tenantService.updateTenant(id, request);
            return ResponseEntity.ok(tenant);
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found for update with ID: {}", id);
            throw e;
        } catch (DuplicateSubdomainException e) {
            log.warn("Duplicate subdomain attempt during update: {}", request.getSubdomain());
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error updating tenant: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete tenant
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String id) {
        log.info("Deleting tenant with ID: {}", id);
        
        try {
            tenantValidationService.validateTenantId(id);
            tenantService.deleteTenant(id);
            return ResponseEntity.noContent().build();
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found for deletion with ID: {}", id);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format for deletion: {}", id);
            throw e;
        }
    }

    /**
     * Get all tenants with pagination
     */
    @GetMapping
    public ResponseEntity<Page<TenantDto>> getAllTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting all tenants with pagination - page: {}, size: {}", page, size);
        
        try {
            tenantValidationService.validatePagination(page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<TenantDto> tenants = tenantService.getAllTenants(pageable);
            return ResponseEntity.ok(tenants);
        } catch (TenantValidationException e) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            throw e;
        }
    }

    /**
     * Get tenants by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TenantDto>> getTenantsByStatus(@PathVariable Tenant.TenantStatus status) {
        log.debug("Getting tenants by status: {}", status);
        
        List<TenantDto> tenants = tenantService.getTenantsByStatus(status);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Get tenants by status with pagination
     */
    @GetMapping("/status/{status}/page")
    public ResponseEntity<Page<TenantDto>> getTenantsByStatusWithPagination(
            @PathVariable Tenant.TenantStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting tenants by status with pagination - status: {}, page: {}, size: {}", status, page, size);
        
        try {
            tenantValidationService.validatePagination(page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<TenantDto> tenants = tenantService.getTenantsByStatus(status, pageable);
            return ResponseEntity.ok(tenants);
        } catch (TenantValidationException e) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            throw e;
        }
    }

    /**
     * Search tenants by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<TenantDto>> searchTenantsByName(@RequestParam String name) {
        log.debug("Searching tenants by name: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<TenantDto> tenants = tenantService.searchTenantsByName(name.trim());
        return ResponseEntity.ok(tenants);
    }

    /**
     * Get active tenants count
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveTenantsCount() {
        log.debug("Getting active tenants count");
        
        long count = tenantService.getActiveTenantsCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Check if tenant exists by ID
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable String id) {
        log.debug("Checking if tenant exists by ID: {}", id);
        
        try {
            tenantValidationService.validateTenantId(id);
            boolean exists = tenantService.existsById(id);
            return ResponseEntity.ok(exists);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", id);
            throw e;
        }
    }

    /**
     * Check if tenant exists by subdomain
     */
    @GetMapping("/subdomain/{subdomain}/exists")
    public ResponseEntity<Boolean> existsBySubdomain(@PathVariable String subdomain) {
        log.debug("Checking if tenant exists by subdomain: {}", subdomain);
        
        boolean exists = tenantService.existsBySubdomain(subdomain);
        return ResponseEntity.ok(exists);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("Health check endpoint called");
        return ResponseEntity.ok("Tenant service is healthy");
    }
} 