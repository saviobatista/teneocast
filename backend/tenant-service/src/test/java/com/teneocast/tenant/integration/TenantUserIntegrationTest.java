package com.teneocast.tenant.integration;

import com.teneocast.tenant.dto.CreateUserRequest;
import com.teneocast.tenant.dto.LoginRequest;
import com.teneocast.tenant.dto.LoginResponse;
import com.teneocast.tenant.dto.TenantUserDto;
import com.teneocast.tenant.dto.UpdateUserRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantUser;
import com.teneocast.tenant.service.AuthService;
import com.teneocast.tenant.service.JwtService;
import com.teneocast.tenant.service.TenantUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integration-test")
class TenantUserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TenantUserService tenantUserService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    void testCreateTenantUser_Success() {
        // Given
        Tenant tenant = createTestTenant();
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        TenantUserDto result = tenantUserService.createTenantUser(tenant.getId(), request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(TenantUserDto.UserRole.MASTER);
        assertThat(result.getTenantId()).isEqualTo(tenant.getId());
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void testCreateTenantUser_DuplicateEmail() {
        // Given
        Tenant tenant = createTestTenant();
        CreateUserRequest request = CreateUserRequest.builder()
                .email("duplicate@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When & Then
        tenantUserService.createTenantUser(tenant.getId(), request);
        assertThrows(Exception.class, () -> tenantUserService.createTenantUser(tenant.getId(), request));
    }

    @Test
    void testGetUserByEmail_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantUser user = createTestUser(tenant, "test@example.com", TenantUser.UserRole.MASTER);

        // When
        TenantUserDto result = tenantUserService.getUserByEmail(tenant.getId(), "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getId()).isEqualTo(user.getId());
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantUser user = createTestUser(tenant);
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("updated@example.com")
                .role(TenantUserDto.UserRole.PRODUCER)
                .isActive(true)
                .build();

        // When
        TenantUserDto result = tenantUserService.updateUser(tenant.getId(), user.getId(), request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getRole()).isEqualTo(TenantUserDto.UserRole.PRODUCER);
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantUser user = createTestUser(tenant);

        // When & Then
        assertDoesNotThrow(() -> tenantUserService.deleteUser(tenant.getId(), user.getId()));
    }

    @Test
    void testGetUsersByTenantId_Success() {
        // Given
        Tenant tenant = createTestTenant();
        createTestUser(tenant, "user1@example.com", TenantUser.UserRole.MASTER);
        createTestUser(tenant, "user2@example.com", TenantUser.UserRole.PRODUCER);
        createTestUser(tenant, "user3@example.com", TenantUser.UserRole.MANAGER);

        // When
        Page<TenantUserDto> result = tenantUserService.getUsersByTenantId(tenant.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testGetUsersByRole_Success() {
        // Given
        Tenant tenant = createTestTenant();
        createTestUser(tenant, "master@example.com", TenantUser.UserRole.MASTER);
        createTestUser(tenant, "producer@example.com", TenantUser.UserRole.PRODUCER);
        createTestUser(tenant, "manager@example.com", TenantUser.UserRole.MANAGER);

        // When
        List<TenantUserDto> result = tenantUserService.getUsersByRole(tenant.getId(), TenantUser.UserRole.MASTER);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(TenantUserDto.UserRole.MASTER);
    }

    @Test
    void testGetActiveUsers_Success() {
        // Given
        Tenant tenant = createTestTenant();
        createTestUser(tenant, "active1@example.com", TenantUser.UserRole.MASTER);
        createTestUser(tenant, "active2@example.com", TenantUser.UserRole.PRODUCER);

        // When
        List<TenantUserDto> result = tenantUserService.getActiveUsers(tenant.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        result.forEach(user -> assertThat(user.getIsActive()).isTrue());
    }

    @Test
    void testGetUsersCount_Success() {
        // Given
        Tenant tenant = createTestTenant();
        createTestUser(tenant, "user1@example.com", TenantUser.UserRole.MASTER);
        createTestUser(tenant, "user2@example.com", TenantUser.UserRole.PRODUCER);

        // When
        long count = tenantUserService.getUsersCount(tenant.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testGetActiveUsersCount_Success() {
        // Given
        Tenant tenant = createTestTenant();
        createTestUser(tenant, "active1@example.com", TenantUser.UserRole.MASTER);
        createTestUser(tenant, "active2@example.com", TenantUser.UserRole.PRODUCER);

        // When
        long count = tenantUserService.getActiveUsersCount(tenant.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testExistsByEmail_Success() {
        // Given
        Tenant tenant = createTestTenant();
        createTestUser(tenant, "exists@example.com", TenantUser.UserRole.MASTER);

        // When
        boolean exists = tenantUserService.existsByEmail(tenant.getId(), "exists@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testAuthenticateUser_Success() {
        // Given
        Tenant tenant = createTestTenant();
        CreateUserRequest createRequest = CreateUserRequest.builder()
                .email("auth@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();
        tenantUserService.createTenantUser(tenant.getId(), createRequest);

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(tenant.getId(), "auth@example.com", "password123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("auth@example.com");
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        // Given
        Tenant tenant = createTestTenant();
        CreateUserRequest createRequest = CreateUserRequest.builder()
                .email("auth@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();
        tenantUserService.createTenantUser(tenant.getId(), createRequest);

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(tenant.getId(), "auth@example.com", "wrongpassword");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testAuthenticationFlow_Success() {
        // Given
        Tenant tenant = createTestTenant();
        CreateUserRequest createRequest = CreateUserRequest.builder()
                .email("auth@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();
        tenantUserService.createTenantUser(tenant.getId(), createRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .tenantId(tenant.getId())
                .email("auth@example.com")
                .password("password123")
                .build();

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("auth@example.com");

        // Verify JWT token
        String username = jwtService.extractUsername(response.getAccessToken());
        assertThat(username).isEqualTo(tenant.getId() + ":" + "auth@example.com");
        assertThat(jwtService.validateToken(response.getAccessToken(), username)).isTrue();
    }

    @Test
    void testTokenRefresh_Success() {
        // Given
        Tenant tenant = createTestTenant();
        CreateUserRequest createRequest = CreateUserRequest.builder()
                .email("refresh@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();
        tenantUserService.createTenantUser(tenant.getId(), createRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .tenantId(tenant.getId())
                .email("refresh@example.com")
                .password("password123")
                .build();

        LoginResponse initialResponse = authService.login(loginRequest);

        // When
        LoginResponse refreshResponse = authService.refreshToken(initialResponse.getRefreshToken());

        // Then
        assertThat(refreshResponse).isNotNull();
        assertThat(refreshResponse.getAccessToken()).isNotNull();
        assertThat(refreshResponse.getAccessToken()).isNotEqualTo(initialResponse.getAccessToken());
    }

    @Test
    void testLogout_Success() {
        // Given
        Tenant tenant = createTestTenant();
        CreateUserRequest createRequest = CreateUserRequest.builder()
                .email("logout@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();
        tenantUserService.createTenantUser(tenant.getId(), createRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .tenantId(tenant.getId())
                .email("logout@example.com")
                .password("password123")
                .build();

        LoginResponse response = authService.login(loginRequest);

        // When & Then
        assertDoesNotThrow(() -> authService.logout(response.getRefreshToken()));
    }
} 