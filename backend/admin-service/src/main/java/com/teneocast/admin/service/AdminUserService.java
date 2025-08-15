package com.teneocast.admin.service;

import com.teneocast.admin.dto.AdminUserDto;
import com.teneocast.admin.dto.CreateAdminUserRequest;
import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.common.dto.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUserDto> getAllAdminUsers() {
        return adminUserRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<AdminUserDto> getAdminUserById(UUID id) {
        return adminUserRepository.findById(id)
                .map(this::mapToDto);
    }

    public Optional<AdminUserDto> getAdminUserByEmail(String email) {
        return adminUserRepository.findByEmailAndIsActiveTrue(email)
                .map(this::mapToDto);
    }

    public AdminUserDto createAdminUser(CreateAdminUserRequest request) {
        if (adminUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Admin user with email " + request.getEmail() + " already exists");
        }

        AdminUser adminUser = AdminUser.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        AdminUser savedUser = adminUserRepository.save(adminUser);
        log.info("Created admin user: {}", savedUser.getEmail());
        
        return mapToDto(savedUser);
    }

    public AdminUserDto updateAdminUser(UUID id, CreateAdminUserRequest request) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found with id: " + id));

        adminUser.setEmail(request.getEmail());
        adminUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        adminUser.setRole(request.getRole());
        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser savedUser = adminUserRepository.save(adminUser);
        log.info("Updated admin user: {}", savedUser.getEmail());
        
        return mapToDto(savedUser);
    }

    public void deactivateAdminUser(UUID id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found with id: " + id));

        adminUser.setIsActive(false);
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(adminUser);
        
        log.info("Deactivated admin user: {}", adminUser.getEmail());
    }

    public void updateLastLogin(UUID id) {
        adminUserRepository.findById(id).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            adminUserRepository.save(user);
        });
    }

    public long getActiveAdminUserCount() {
        return adminUserRepository.countActiveUsers();
    }

    public List<AdminUserDto> getAdminUsersByRole(UserRole role) {
        return adminUserRepository.findByRole(role)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AdminUserDto mapToDto(AdminUser adminUser) {
        return AdminUserDto.builder()
                .id(adminUser.getId())
                .email(adminUser.getEmail())
                .role(adminUser.getRole())
                .isActive(adminUser.getIsActive())
                .createdAt(adminUser.getCreatedAt())
                .updatedAt(adminUser.getUpdatedAt())
                .lastLoginAt(adminUser.getLastLoginAt())
                .build();
    }
}
