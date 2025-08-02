package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.CreateTenantRequest;
import com.teneocast.tenant.dto.TenantDto;
import com.teneocast.tenant.dto.UpdateTenantRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.exception.DuplicateSubdomainException;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantValidationService tenantValidationService;

    /**
     * Create a new tenant
     */
    public TenantDto createTenant(CreateTenantRequest request) {
        log.info("Creating tenant with subdomain: {}", request.getSubdomain());
        
        // Validate request
        tenantValidationService.validateCreateTenantRequest(request);
        
        // Check for duplicate subdomain
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new DuplicateSubdomainException("Subdomain already exists: " + request.getSubdomain());
        }
        
        // Create tenant entity
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .subdomain(request.getSubdomain())
                .status(Tenant.TenantStatus.ACTIVE)
                .build();
        
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Created tenant with ID: {}", savedTenant.getId());
        
        return mapToDto(savedTenant);
    }

    /**
     * Get tenant by ID
     */
    @Transactional(readOnly = true)
    public TenantDto getTenantById(String id) {
        log.debug("Getting tenant by ID: {}", id);
        
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + id));
        
        return mapToDto(tenant);
    }

    /**
     * Get tenant by subdomain
     */
    @Transactional(readOnly = true)
    public TenantDto getTenantBySubdomain(String subdomain) {
        log.debug("Getting tenant by subdomain: {}", subdomain);
        
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with subdomain: " + subdomain));
        
        return mapToDto(tenant);
    }

    /**
     * Update tenant
     */
    public TenantDto updateTenant(String id, UpdateTenantRequest request) {
        log.info("Updating tenant with ID: {}", id);
        
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + id));
        
        // Validate request
        tenantValidationService.validateUpdateTenantRequest(request);
        
        // Check for duplicate subdomain if changing
        if (request.getSubdomain() != null && !request.getSubdomain().equals(tenant.getSubdomain())) {
            if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
                throw new DuplicateSubdomainException("Subdomain already exists: " + request.getSubdomain());
            }
        }
        
        // Update fields
        if (request.getName() != null) {
            tenant.setName(request.getName());
        }
        if (request.getSubdomain() != null) {
            tenant.setSubdomain(request.getSubdomain());
        }
        if (request.getStatus() != null) {
            tenant.setStatus(Tenant.TenantStatus.valueOf(request.getStatus().name()));
        }
        
        tenant.setUpdatedAt(LocalDateTime.now());
        Tenant savedTenant = tenantRepository.save(tenant);
        
        log.info("Updated tenant with ID: {}", savedTenant.getId());
        return mapToDto(savedTenant);
    }

    /**
     * Delete tenant
     */
    public void deleteTenant(String id) {
        log.info("Deleting tenant with ID: {}", id);
        
        if (!tenantRepository.existsById(id)) {
            throw new TenantNotFoundException("Tenant not found with ID: " + id);
        }
        
        tenantRepository.deleteById(id);
        log.info("Deleted tenant with ID: {}", id);
    }

    /**
     * Get all tenants with pagination
     */
    @Transactional(readOnly = true)
    public Page<TenantDto> getAllTenants(Pageable pageable) {
        log.debug("Getting all tenants with pagination: {}", pageable);
        
        Page<Tenant> tenants = tenantRepository.findAll(pageable);
        return tenants.map(this::mapToDto);
    }

    /**
     * Get tenants by status
     */
    @Transactional(readOnly = true)
    public List<TenantDto> getTenantsByStatus(Tenant.TenantStatus status) {
        log.debug("Getting tenants by status: {}", status);
        
        List<Tenant> tenants = tenantRepository.findByStatus(status);
        return tenants.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get tenants by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<TenantDto> getTenantsByStatus(Tenant.TenantStatus status, Pageable pageable) {
        log.debug("Getting tenants by status: {} with pagination: {}", status, pageable);
        
        Page<Tenant> tenants = tenantRepository.findByStatus(status, pageable);
        return tenants.map(this::mapToDto);
    }

    /**
     * Search tenants by name
     */
    @Transactional(readOnly = true)
    public List<TenantDto> searchTenantsByName(String name) {
        log.debug("Searching tenants by name: {}", name);
        
        List<Tenant> tenants = tenantRepository.findByNameContainingIgnoreCase(name);
        return tenants.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get active tenants count
     */
    @Transactional(readOnly = true)
    public long getActiveTenantsCount() {
        log.debug("Getting active tenants count");
        return tenantRepository.countByStatus(Tenant.TenantStatus.ACTIVE);
    }

    /**
     * Check if tenant exists by ID
     */
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        return tenantRepository.existsById(id);
    }

    /**
     * Check if tenant exists by subdomain
     */
    @Transactional(readOnly = true)
    public boolean existsBySubdomain(String subdomain) {
        return tenantRepository.existsBySubdomain(subdomain);
    }

    /**
     * Get tenant entity by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantEntityById(String id) {
        return tenantRepository.findById(id);
    }

    /**
     * Map entity to DTO
     */
    private TenantDto mapToDto(Tenant tenant) {
        return TenantDto.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .status(TenantDto.TenantStatus.valueOf(tenant.getStatus().name()))
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
} 