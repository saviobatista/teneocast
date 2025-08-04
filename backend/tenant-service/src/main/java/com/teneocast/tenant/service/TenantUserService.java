package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.CreateUserRequest;
import com.teneocast.tenant.dto.TenantUserDto;
import com.teneocast.tenant.dto.UpdateUserRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantUser;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.repository.TenantRepository;
import com.teneocast.tenant.repository.TenantUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantUserService {

    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;
    private final TenantValidationService tenantValidationService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new tenant user
     */
    public TenantUserDto createTenantUser(String tenantId, CreateUserRequest request) {
        log.info("Creating user for tenant: {}", tenantId);
        
        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + tenantId));
        
        // Validate request
        validateCreateUserRequest(request);
        
        // Check for duplicate email within tenant
        if (tenantUserRepository.findByTenantIdAndEmail(tenantId, request.getEmail()).isPresent()) {
            throw new TenantValidationException("User with email already exists in this tenant: " + request.getEmail());
        }
        
        // Create user entity
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .role(TenantUser.UserRole.valueOf(request.getRole().name()))
                .isActive(true)
                .build();
        
        // Encode password if provided
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        
        TenantUser savedUser = tenantUserRepository.save(user);
        log.info("Created user with ID: {} for tenant: {}", savedUser.getId(), tenantId);
        
        return mapToDto(savedUser);
    }

    /**
     * Get user by email within tenant
     */
    @Transactional(readOnly = true)
    public TenantUserDto getUserByEmail(String tenantId, String email) {
        log.debug("Getting user by email: {} for tenant: {}", email, tenantId);
        
        TenantUser user = tenantUserRepository.findByTenantIdAndEmail(tenantId, email)
                .orElseThrow(() -> new TenantNotFoundException("User not found with email: " + email + " in tenant: " + tenantId));
        
        return mapToDto(user);
    }

    /**
     * Update user
     */
    public TenantUserDto updateUser(String tenantId, String userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {} for tenant: {}", userId, tenantId);
        
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new TenantNotFoundException("User not found with ID: " + userId));
        
        // Validate that user belongs to the tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new TenantNotFoundException("User not found with ID: " + userId + " in tenant: " + tenantId);
        }
        
        // Validate request
        validateUpdateUserRequest(request);
        
        // Check for duplicate email if changing
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (tenantUserRepository.findByTenantIdAndEmail(tenantId, request.getEmail()).isPresent()) {
                throw new TenantValidationException("User with email already exists in this tenant: " + request.getEmail());
            }
        }
        
        // Update fields
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(TenantUser.UserRole.valueOf(request.getRole().name()));
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        // Update password if provided
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        TenantUser savedUser = tenantUserRepository.save(user);
        
        log.info("Updated user with ID: {} for tenant: {}", savedUser.getId(), tenantId);
        return mapToDto(savedUser);
    }

    /**
     * Delete user
     */
    public void deleteUser(String tenantId, String userId) {
        log.info("Deleting user with ID: {} for tenant: {}", userId, tenantId);
        
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new TenantNotFoundException("User not found with ID: " + userId));
        
        // Validate that user belongs to the tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new TenantNotFoundException("User not found with ID: " + userId + " in tenant: " + tenantId);
        }
        
        tenantUserRepository.delete(user);
        log.info("Deleted user with ID: {} for tenant: {}", userId, tenantId);
    }

    /**
     * Get all users for tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<TenantUserDto> getUsersByTenantId(String tenantId, Pageable pageable) {
        log.debug("Getting users for tenant: {} with pagination: {}", tenantId, pageable);
        
        Page<TenantUser> users = tenantUserRepository.findByTenantId(tenantId, pageable);
        return users.map(this::mapToDto);
    }

    /**
     * Get users by role within tenant
     */
    @Transactional(readOnly = true)
    public List<TenantUserDto> getUsersByRole(String tenantId, TenantUser.UserRole role) {
        log.debug("Getting users by role: {} for tenant: {}", role, tenantId);
        
        List<TenantUser> users = tenantUserRepository.findByTenantIdAndRole(tenantId, role);
        return users.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get active users for tenant
     */
    @Transactional(readOnly = true)
    public List<TenantUserDto> getActiveUsers(String tenantId) {
        log.debug("Getting active users for tenant: {}", tenantId);
        
        List<TenantUser> users = tenantUserRepository.findByTenantIdAndIsActive(tenantId, true);
        return users.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get users count for tenant
     */
    @Transactional(readOnly = true)
    public long getUsersCount(String tenantId) {
        log.debug("Getting users count for tenant: {}", tenantId);
        return tenantUserRepository.countByTenantId(tenantId);
    }

    /**
     * Get active users count for tenant
     */
    @Transactional(readOnly = true)
    public long getActiveUsersCount(String tenantId) {
        log.debug("Getting active users count for tenant: {}", tenantId);
        return tenantUserRepository.countByTenantIdAndIsActive(tenantId, true);
    }

    /**
     * Check if user exists by email within tenant
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String tenantId, String email) {
        return tenantUserRepository.findByTenantIdAndEmail(tenantId, email).isPresent();
    }

    /**
     * Authenticate user
     */
    @Transactional(readOnly = true)
    public Optional<TenantUserDto> authenticateUser(String tenantId, String email, String password) {
        log.debug("Authenticating user with email: {} for tenant: {}", email, tenantId);
        
        Optional<TenantUser> user = tenantUserRepository.findByTenantIdAndEmail(tenantId, email);
        
        if (user.isPresent() && user.get().getIsActive()) {
            TenantUser foundUser = user.get();
            if (passwordEncoder.matches(password, foundUser.getPasswordHash())) {
                return Optional.of(mapToDto(foundUser));
            }
        }
        
        return Optional.empty();
    }

    /**
     * Validate create user request
     */
    private void validateCreateUserRequest(CreateUserRequest request) {
        if (request == null) {
            throw new TenantValidationException("Create user request cannot be null");
        }
        
        if (!tenantValidationService.isValidEmail(request.getEmail())) {
            throw new TenantValidationException("Invalid email format: " + request.getEmail());
        }
        
        if (request.getPassword() != null && request.getPassword().length() < 8) {
            throw new TenantValidationException("Password must be at least 8 characters long");
        }
    }

    /**
     * Validate update user request
     */
    private void validateUpdateUserRequest(UpdateUserRequest request) {
        if (request == null) {
            throw new TenantValidationException("Update user request cannot be null");
        }
        
        if (request.getEmail() != null && !tenantValidationService.isValidEmail(request.getEmail())) {
            throw new TenantValidationException("Invalid email format: " + request.getEmail());
        }
        
        if (request.getPassword() != null && request.getPassword().length() < 8) {
            throw new TenantValidationException("Password must be at least 8 characters long");
        }
    }

    /**
     * Map entity to DTO
     */
    public TenantUserDto mapToDto(TenantUser user) {
        return TenantUserDto.builder()
                .id(user.getId())
                .tenantId(user.getTenant().getId())
                .email(user.getEmail())
                .role(TenantUserDto.UserRole.valueOf(user.getRole().name()))
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 