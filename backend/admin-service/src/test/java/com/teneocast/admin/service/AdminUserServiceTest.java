package com.teneocast.admin.service;

import com.teneocast.admin.dto.CreateAdminUserRequest;
import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.common.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(adminUserRepository, passwordEncoder);
    }

    @Test
    void adminUserService_ShouldBeInstantiated() {
        assertNotNull(adminUserService);
    }

    @Test
    void createAdminUser_ShouldCreateSuccessfully() {
        // Given
        CreateAdminUserRequest request = CreateAdminUserRequest.builder()
                .email("test@teneocast.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        AdminUser savedUser = AdminUser.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash("encodedPassword")
                .role(request.getRole())
                .isActive(true)
                .build();

        when(adminUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(adminUserRepository.save(any(AdminUser.class))).thenReturn(savedUser);

        // When
        var result = adminUserService.createAdminUser(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getRole(), result.getRole());
        assertTrue(result.getIsActive());

        verify(adminUserRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(adminUserRepository).save(any(AdminUser.class));
    }

    @Test
    void createAdminUser_ShouldThrowException_WhenEmailExists() {
        // Given
        CreateAdminUserRequest request = CreateAdminUserRequest.builder()
                .email("existing@teneocast.com")
                .password("password123")
                .role(UserRole.OPERATOR)
                .build();

        when(adminUserRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> adminUserService.createAdminUser(request));
        verify(adminUserRepository).existsByEmail(request.getEmail());
        verify(adminUserRepository, never()).save(any());
    }
}
