package com.teneocast.tenant.controller;

import com.teneocast.tenant.dto.CreateUserRequest;
import com.teneocast.tenant.dto.TenantUserDto;
import com.teneocast.tenant.dto.UpdateUserRequest;
import com.teneocast.tenant.entity.TenantUser;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantUserService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
@RequiredArgsConstructor
@Slf4j
public class TenantUserController {

    private final TenantUserService tenantUserService;
    private final TenantValidationService tenantValidationService;

    /**
     * Create a new user for tenant
     */
    @PostMapping
    public ResponseEntity<TenantUserDto> createUser(@PathVariable String tenantId,
                                                  @Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantUserDto user = tenantUserService.createTenantUser(tenantId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found for user creation: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error creating user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get user by email within tenant
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<TenantUserDto> getUserByEmail(@PathVariable String tenantId,
                                                      @PathVariable String email) {
        log.debug("Getting user by email: {} for tenant: {}", email, tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantUserDto user = tenantUserService.getUserByEmail(tenantId, email);
            return ResponseEntity.ok(user);
        } catch (TenantNotFoundException e) {
            log.warn("User not found with email: {} in tenant: {}", email, tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<TenantUserDto> updateUser(@PathVariable String tenantId,
                                                  @PathVariable String userId,
                                                  @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user with ID: {} for tenant: {}", userId, tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantUserDto user = tenantUserService.updateUser(tenantId, userId, request);
            return ResponseEntity.ok(user);
        } catch (TenantNotFoundException e) {
            log.warn("User not found for update with ID: {} in tenant: {}", userId, tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error updating user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String tenantId,
                                         @PathVariable String userId) {
        log.info("Deleting user with ID: {} for tenant: {}", userId, tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            tenantUserService.deleteUser(tenantId, userId);
            return ResponseEntity.noContent().build();
        } catch (TenantNotFoundException e) {
            log.warn("User not found for deletion with ID: {} in tenant: {}", userId, tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format for deletion: {}", tenantId);
            throw e;
        }
    }

    /**
     * Get all users for tenant with pagination
     */
    @GetMapping
    public ResponseEntity<Page<TenantUserDto>> getUsersByTenantId(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting users for tenant: {} with pagination - page: {}, size: {}", tenantId, page, size);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            tenantValidationService.validatePagination(page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<TenantUserDto> users = tenantUserService.getUsersByTenantId(tenantId, pageable);
            return ResponseEntity.ok(users);
        } catch (TenantValidationException e) {
            log.warn("Validation error getting users: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get users by role within tenant
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<TenantUserDto>> getUsersByRole(@PathVariable String tenantId,
                                                            @PathVariable TenantUser.UserRole role) {
        log.debug("Getting users by role: {} for tenant: {}", role, tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            List<TenantUserDto> users = tenantUserService.getUsersByRole(tenantId, role);
            return ResponseEntity.ok(users);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Get active users for tenant
     */
    @GetMapping("/active")
    public ResponseEntity<List<TenantUserDto>> getActiveUsers(@PathVariable String tenantId) {
        log.debug("Getting active users for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            List<TenantUserDto> users = tenantUserService.getActiveUsers(tenantId);
            return ResponseEntity.ok(users);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Get users count for tenant
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getUsersCount(@PathVariable String tenantId) {
        log.debug("Getting users count for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            long count = tenantUserService.getUsersCount(tenantId);
            return ResponseEntity.ok(count);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Get active users count for tenant
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveUsersCount(@PathVariable String tenantId) {
        log.debug("Getting active users count for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            long count = tenantUserService.getActiveUsersCount(tenantId);
            return ResponseEntity.ok(count);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Check if user exists by email within tenant
     */
    @GetMapping("/email/{email}/exists")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String tenantId,
                                               @PathVariable String email) {
        log.debug("Checking if user exists by email: {} for tenant: {}", email, tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            boolean exists = tenantUserService.existsByEmail(tenantId, email);
            return ResponseEntity.ok(exists);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Authenticate user
     */
    @PostMapping("/authenticate")
    public ResponseEntity<TenantUserDto> authenticateUser(@PathVariable String tenantId,
                                                        @RequestParam String email,
                                                        @RequestParam String password) {
        log.debug("Authenticating user with email: {} for tenant: {}", email, tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            Optional<TenantUserDto> user = tenantUserService.authenticateUser(tenantId, email, password);
            
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health(@PathVariable String tenantId) {
        log.debug("Health check endpoint called for tenant: {}", tenantId);
        return ResponseEntity.ok("Tenant user service is healthy for tenant: " + tenantId);
    }
} 