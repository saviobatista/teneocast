package com.teneocast.admin.controller;

import com.teneocast.admin.dto.AdminUserDto;
import com.teneocast.admin.dto.CreateAdminUserRequest;
import com.teneocast.admin.service.AdminUserService;
import com.teneocast.common.dto.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "Operations for managing admin users (ROOT/OPERATOR)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> getAllAdminUsers() {
        log.info("Fetching all admin users");
        List<AdminUserDto> users = adminUserService.getAllAdminUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDto> getAdminUserById(@PathVariable UUID id) {
        log.info("Fetching admin user with id: {}", id);
        return adminUserService.getAdminUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<AdminUserDto> getAdminUserByEmail(@PathVariable String email) {
        log.info("Fetching admin user with email: {}", email);
        return adminUserService.getAdminUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AdminUserDto> createAdminUser(@Valid @RequestBody CreateAdminUserRequest request) {
        log.info("Creating admin user with email: {}", request.getEmail());
        try {
            AdminUserDto createdUser = adminUserService.createAdminUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create admin user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDto> updateAdminUser(@PathVariable UUID id, 
                                                      @Valid @RequestBody CreateAdminUserRequest request) {
        log.info("Updating admin user with id: {}", id);
        try {
            AdminUserDto updatedUser = adminUserService.updateAdminUser(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update admin user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateAdminUser(@PathVariable UUID id) {
        log.info("Deactivating admin user with id: {}", id);
        try {
            adminUserService.deactivateAdminUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to deactivate admin user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<AdminUserDto>> getAdminUsersByRole(@PathVariable UserRole role) {
        log.info("Fetching admin users with role: {}", role);
        List<AdminUserDto> users = adminUserService.getAdminUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getActiveAdminUserCount() {
        log.info("Fetching active admin user count");
        long count = adminUserService.getActiveAdminUserCount();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/last-login")
    public ResponseEntity<Void> updateLastLogin(@PathVariable UUID id) {
        log.info("Updating last login for admin user with id: {}", id);
        adminUserService.updateLastLogin(id);
        return ResponseEntity.ok().build();
    }
}
