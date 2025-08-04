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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantUserServiceTest {

    @Mock
    private TenantUserRepository tenantUserRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantValidationService tenantValidationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantUserService tenantUserService;

    private Tenant testTenant;
    private TenantUser testUser;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testTenant = Tenant.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        testUser = TenantUser.builder()
                .id(UUID.randomUUID().toString())
                .tenant(testTenant)
                .email("test@example.com")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createUserRequest = CreateUserRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .email("updated@example.com")
                .role(TenantUserDto.UserRole.PRODUCER)
                .isActive(true)
                .build();
    }

    @Test
    void testCreateTenantUser_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), createUserRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encoded-password");
        when(tenantUserRepository.save(any(TenantUser.class))).thenReturn(testUser);
        when(tenantValidationService.isValidEmail(createUserRequest.getEmail())).thenReturn(true);

        // When
        TenantUserDto result = tenantUserService.createTenantUser(testTenant.getId(), createUserRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getRole().name(), result.getRole().name());
        assertTrue(result.getIsActive());

        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), createUserRequest.getEmail());
        verify(passwordEncoder).encode(createUserRequest.getPassword());
        verify(tenantUserRepository).save(any(TenantUser.class));
    }

    @Test
    void testCreateTenantUser_TenantNotFound() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.createTenantUser(testTenant.getId(), createUserRequest));

        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantUserRepository, never()).save(any());
    }

    @Test
    void testCreateTenantUser_DuplicateEmail() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), createUserRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantValidationService.isValidEmail(createUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantUserService.createTenantUser(testTenant.getId(), createUserRequest));

        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), createUserRequest.getEmail());
        verify(tenantUserRepository, never()).save(any());
    }

    @Test
    void testGetUserByEmail_Success() {
        // Given
        String email = "test@example.com";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.of(testUser));

        // When
        TenantUserDto result = tenantUserService.getUserByEmail(testTenant.getId(), email);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
    }

    @Test
    void testGetUserByEmail_NotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.getUserByEmail(testTenant.getId(), email));

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), updateUserRequest.getEmail())).thenReturn(Optional.empty());
        when(tenantUserRepository.save(any(TenantUser.class))).thenReturn(testUser);
        when(tenantValidationService.isValidEmail(updateUserRequest.getEmail())).thenReturn(true);

        // When
        TenantUserDto result = tenantUserService.updateUser(testTenant.getId(), testUser.getId(), updateUserRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());

        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), updateUserRequest.getEmail());
        verify(tenantUserRepository).save(any(TenantUser.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Given
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.updateUser(testTenant.getId(), testUser.getId(), updateUserRequest));

        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_WrongTenant() {
        // Given
        String wrongTenantId = UUID.randomUUID().toString();
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.updateUser(wrongTenantId, testUser.getId(), updateUserRequest));

        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_DuplicateEmail() {
        // Given
        TenantUser existingUser = TenantUser.builder()
                .id(UUID.randomUUID().toString())
                .tenant(testTenant)
                .email(updateUserRequest.getEmail())
                .build();
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), updateUserRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(tenantValidationService.isValidEmail(updateUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantUserService.updateUser(testTenant.getId(), testUser.getId(), updateUserRequest));

        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), updateUserRequest.getEmail());
        verify(tenantUserRepository, never()).save(any());
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        doNothing().when(tenantUserRepository).delete(testUser);

        // When
        tenantUserService.deleteUser(testTenant.getId(), testUser.getId());

        // Then
        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository).delete(testUser);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Given
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.deleteUser(testTenant.getId(), testUser.getId()));

        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository, never()).delete(any());
    }

    @Test
    void testDeleteUser_WrongTenant() {
        // Given
        String wrongTenantId = UUID.randomUUID().toString();
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.deleteUser(wrongTenantId, testUser.getId()));

        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository, never()).delete(any());
    }

    @Test
    void testGetUsersByTenantId_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TenantUser> userPage = new PageImpl<>(List.of(testUser));
        when(tenantUserRepository.findByTenantId(testTenant.getId(), pageable)).thenReturn(userPage);

        // When
        Page<TenantUserDto> result = tenantUserService.getUsersByTenantId(testTenant.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getId(), result.getContent().get(0).getId());

        verify(tenantUserRepository).findByTenantId(testTenant.getId(), pageable);
    }

    @Test
    void testGetUsersByRole_Success() {
        // Given
        TenantUser.UserRole role = TenantUser.UserRole.MASTER;
        when(tenantUserRepository.findByTenantIdAndRole(testTenant.getId(), role)).thenReturn(List.of(testUser));

        // When
        List<TenantUserDto> result = tenantUserService.getUsersByRole(testTenant.getId(), role);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());

        verify(tenantUserRepository).findByTenantIdAndRole(testTenant.getId(), role);
    }

    @Test
    void testGetActiveUsers_Success() {
        // Given
        when(tenantUserRepository.findByTenantIdAndIsActive(testTenant.getId(), true)).thenReturn(List.of(testUser));

        // When
        List<TenantUserDto> result = tenantUserService.getActiveUsers(testTenant.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());

        verify(tenantUserRepository).findByTenantIdAndIsActive(testTenant.getId(), true);
    }

    @Test
    void testGetUsersCount_Success() {
        // Given
        when(tenantUserRepository.countByTenantId(testTenant.getId())).thenReturn(5L);

        // When
        long result = tenantUserService.getUsersCount(testTenant.getId());

        // Then
        assertEquals(5L, result);

        verify(tenantUserRepository).countByTenantId(testTenant.getId());
    }

    @Test
    void testGetActiveUsersCount_Success() {
        // Given
        when(tenantUserRepository.countByTenantIdAndIsActive(testTenant.getId(), true)).thenReturn(3L);

        // When
        long result = tenantUserService.getActiveUsersCount(testTenant.getId());

        // Then
        assertEquals(3L, result);

        verify(tenantUserRepository).countByTenantIdAndIsActive(testTenant.getId(), true);
    }

    @Test
    void testExistsByEmail_Success() {
        // Given
        String email = "test@example.com";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.of(testUser));

        // When
        boolean result = tenantUserService.existsByEmail(testTenant.getId(), email);

        // Then
        assertTrue(result);

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
    }

    @Test
    void testExistsByEmail_NotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.empty());

        // When
        boolean result = tenantUserService.existsByEmail(testTenant.getId(), email);

        // Then
        assertFalse(result);

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
    }

    @Test
    void testAuthenticateUser_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), email, password);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
        verify(passwordEncoder).matches(password, testUser.getPasswordHash());
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password123";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.empty());

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), email, password);

        // Then
        assertFalse(result.isPresent());

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testAuthenticateUser_InactiveUser() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        testUser.setIsActive(false);
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.of(testUser));

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), email, password);

        // Then
        assertFalse(result.isPresent());

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(false);

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), email, password);

        // Then
        assertFalse(result.isPresent());

        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), email);
        verify(passwordEncoder).matches(password, testUser.getPasswordHash());
    }
} 