package com.teneocast.admin.service;

import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.common.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(adminUserRepository);
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "test_jwt_secret_key_for_testing_only");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 300000L);
    }

    @Test
    void jwtService_ShouldBeInstantiated() {
        assertNotNull(jwtService);
    }

    @Test
    void validateTokenAndGetUser_ShouldReturnEmpty_WhenTokenIsNull() {
        // When
        Optional<AdminUser> result = jwtService.validateTokenAndGetUser(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void validateTokenAndGetUser_ShouldReturnEmpty_WhenTokenDoesNotStartWithBearer() {
        // When
        Optional<AdminUser> result = jwtService.validateTokenAndGetUser("InvalidToken");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void extractUserId_ShouldReturnEmpty_WhenTokenIsNull() {
        // When
        Optional<String> result = jwtService.extractUserId(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void extractRole_ShouldReturnEmpty_WhenTokenIsNull() {
        // When
        Optional<String> result = jwtService.extractRole(null);

        // Then
        assertTrue(result.isEmpty());
    }
}
