package com.teneocast.tenant.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TenantUserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testTenantUserBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        
        // When
        TenantUser user = TenantUser.builder()
                .id("user-id")
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .build();

        // Then
        assertNotNull(user);
        assertEquals("user-id", user.getId());
        assertEquals(tenant, user.getTenant());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashed-password", user.getPasswordHash());
        assertEquals(TenantUser.UserRole.MASTER, user.getRole());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals(now, user.getLastLoginAt());
    }

    @Test
    void testTenantUserDefaultValues() {
        // When
        TenantUser user = new TenantUser();

        // Then
        assertNull(user.getId());
        assertNull(user.getTenant());
        assertNull(user.getEmail());
        assertNull(user.getPasswordHash());
        assertNull(user.getRole());
        assertTrue(user.getIsActive()); // @Builder.Default sets this to true
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        assertNull(user.getLastLoginAt());
    }

    @Test
    void testTenantUserPrePersist() {
        // Given
        TenantUser user = new TenantUser();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(TenantUser.UserRole.MASTER);

        // When
        user.prePersist();

        // Then
        assertTrue(user.getIsActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testTenantUserPreUpdate() {
        // Given
        TenantUser user = TenantUser.builder()
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(1);
        user.setUpdatedAt(originalUpdatedAt);

        // When
        user.preUpdate();

        // Then
        assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testTenantUserValidationSuccess() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<TenantUser>> violations = validator.validate(user);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantUserValidationTenantRequired() {
        // Given
        TenantUser user = TenantUser.builder()
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<TenantUser>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tenant")));
    }

    @Test
    void testTenantUserValidationEmailRequired() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<TenantUser>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testTenantUserValidationEmailInvalid() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email("invalid-email")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<TenantUser>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testTenantUserValidationEmailValid() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<TenantUser>> violations = validator.validate(user);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantUserValidationPasswordHashRequired() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email("test@example.com")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<TenantUser>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("passwordHash")));
    }

    @Test
    void testTenantUserValidationRoleRequired() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("hashed-password")
                .build();

        // When
        Set<ConstraintViolation<TenantUser>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    void testTenantUserEqualsAndHashCode() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user1 = TenantUser.builder()
                .id("user-id")
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        TenantUser user2 = TenantUser.builder()
                .id("user-id")
                .tenant(tenant)
                .email("different@example.com")
                .passwordHash("different-hash")
                .role(TenantUser.UserRole.PRODUCER)
                .build();

        TenantUser user3 = TenantUser.builder()
                .id("different-id")
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testTenantUserRoleEnum() {
        // Given & When & Then
        assertEquals(3, TenantUser.UserRole.values().length);
        assertNotNull(TenantUser.UserRole.valueOf("MASTER"));
        assertNotNull(TenantUser.UserRole.valueOf("PRODUCER"));
        assertNotNull(TenantUser.UserRole.valueOf("MANAGER"));
    }

    @Test
    void testTenantUserToString() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantUser user = TenantUser.builder()
                .id("user-id")
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(TenantUser.UserRole.MASTER)
                .build();

        // When
        String result = user.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test@example.com"));
        assertTrue(result.contains("MASTER"));
    }
} 