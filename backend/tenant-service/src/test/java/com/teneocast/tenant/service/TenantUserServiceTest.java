package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.TenantUserDto;
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
    private TenantUserDto createUserRequest;
    private TenantUserDto updateUserRequest;

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

        createUserRequest = TenantUserDto.builder()
                .email("newuser@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        updateUserRequest = TenantUserDto.builder()
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
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
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
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void testGetUserByEmail_Success() {
        // Given
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.of(testUser));

        // When
        TenantUserDto result = tenantUserService.getUserByEmail(testTenant.getId(), testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        // Given
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.getUserByEmail(testTenant.getId(), testUser.getEmail()));
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
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
        verify(tenantUserRepository).save(any(TenantUser.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Given
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.updateUser(testTenant.getId(), testUser.getId(), updateUserRequest));
        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void testUpdateUser_WrongTenant() {
        // Given
        Tenant wrongTenant = Tenant.builder().id(UUID.randomUUID().toString()).build();
        TenantUser userWithWrongTenant = TenantUser.builder()
                .id(testUser.getId())
                .tenant(wrongTenant)
                .build();
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(userWithWrongTenant));

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.updateUser(testTenant.getId(), testUser.getId(), updateUserRequest));
        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void testUpdateUser_DuplicateEmail() {
        // Given
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), updateUserRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantValidationService.isValidEmail(updateUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantUserService.updateUser(testTenant.getId(), testUser.getId(), updateUserRequest));
        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), updateUserRequest.getEmail());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

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
        verify(tenantUserRepository, never()).delete(any(TenantUser.class));
    }

    @Test
    void testDeleteUser_WrongTenant() {
        // Given
        Tenant wrongTenant = Tenant.builder().id(UUID.randomUUID().toString()).build();
        TenantUser userWithWrongTenant = TenantUser.builder()
                .id(testUser.getId())
                .tenant(wrongTenant)
                .build();
        when(tenantUserRepository.findById(testUser.getId())).thenReturn(Optional.of(userWithWrongTenant));

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantUserService.deleteUser(testTenant.getId(), testUser.getId()));
        verify(tenantUserRepository).findById(testUser.getId());
        verify(tenantUserRepository, never()).delete(any(TenantUser.class));
    }

    @Test
    void testGetUsersByTenantId_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TenantUser> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
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
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.of(testUser));

        // When
        boolean result = tenantUserService.existsByEmail(testTenant.getId(), testUser.getEmail());

        // Then
        assertTrue(result);
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
    }

    @Test
    void testExistsByEmail_NotFound() {
        // Given
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.empty());

        // When
        boolean result = tenantUserService.existsByEmail(testTenant.getId(), testUser.getEmail());

        // Then
        assertFalse(result);
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
    }

    @Test
    void testAuthenticateUser_Success() {
        // Given
        String password = "password123";
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), testUser.getEmail(), password);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
        verify(passwordEncoder).matches(password, testUser.getPasswordHash());
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        // Given
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.empty());

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), testUser.getEmail(), "password");

        // Then
        assertFalse(result.isPresent());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testAuthenticateUser_InactiveUser() {
        // Given
        TenantUser inactiveUser = TenantUser.builder()
                .id(testUser.getId())
                .tenant(testTenant)
                .email(testUser.getEmail())
                .isActive(false)
                .passwordHash("encoded-password")
                .build();
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.of(inactiveUser));

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), testUser.getEmail(), "password");

        // Then
        assertFalse(result.isPresent());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        // Given
        when(tenantUserRepository.findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

        // When
        Optional<TenantUserDto> result = tenantUserService.authenticateUser(testTenant.getId(), testUser.getEmail(), "wrongpassword");

        // Then
        assertFalse(result.isPresent());
        verify(tenantUserRepository).findByTenantIdAndEmail(testTenant.getId(), testUser.getEmail());
        verify(passwordEncoder).matches("wrongpassword", testUser.getPasswordHash());
    }
} 