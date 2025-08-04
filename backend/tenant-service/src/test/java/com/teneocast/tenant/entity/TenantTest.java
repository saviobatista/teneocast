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
class TenantTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testTenantBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        Tenant tenant = Tenant.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .preferences("{\"key\":\"value\"}")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(tenant);
        assertEquals("test-id", tenant.getId());
        assertEquals("Test Tenant", tenant.getName());
        assertEquals("test-tenant", tenant.getSubdomain());
        assertEquals(Tenant.TenantStatus.ACTIVE, tenant.getStatus());
        assertEquals("{\"key\":\"value\"}", tenant.getPreferences());
        assertEquals(now, tenant.getCreatedAt());
        assertEquals(now, tenant.getUpdatedAt());
    }

    @Test
    void testTenantDefaultValues() {
        // When
        Tenant tenant = new Tenant();

        // Then
        assertNull(tenant.getId());
        assertNull(tenant.getName());
        assertNull(tenant.getSubdomain());
        assertEquals(Tenant.TenantStatus.ACTIVE, tenant.getStatus()); // @Builder.Default sets this
        assertNull(tenant.getPreferences());
        assertNull(tenant.getCreatedAt());
        assertNull(tenant.getUpdatedAt());
    }

    @Test
    void testTenantPrePersist() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setName("Test Tenant");
        tenant.setSubdomain("test-tenant");

        // When
        tenant.prePersist();

        // Then
        assertEquals(Tenant.TenantStatus.ACTIVE, tenant.getStatus());
        assertNotNull(tenant.getCreatedAt());
        assertNotNull(tenant.getUpdatedAt());
    }

    @Test
    void testTenantPreUpdate() {
        // Given
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(1);
        tenant.setUpdatedAt(originalUpdatedAt);

        // When
        tenant.preUpdate();

        // Then
        assertTrue(tenant.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testTenantValidationSuccess() {
        // Given
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantValidationNameRequired() {
        // Given
        Tenant tenant = Tenant.builder()
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testTenantValidationNameTooShort() {
        // Given
        Tenant tenant = Tenant.builder()
                .name("A")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testTenantValidationNameTooLong() {
        // Given
        String longName = "A".repeat(256);
        Tenant tenant = Tenant.builder()
                .name(longName)
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testTenantValidationSubdomainRequired() {
        // Given
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testTenantValidationSubdomainTooShort() {
        // Given
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .subdomain("ab")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testTenantValidationSubdomainInvalidCharacters() {
        // Given
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .subdomain("test_tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testTenantValidationSubdomainValidCharacters() {
        // Given
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant-123")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantEqualsAndHashCode() {
        // Given
        Tenant tenant1 = Tenant.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        Tenant tenant2 = Tenant.builder()
                .id("test-id")
                .name("Different Name")
                .subdomain("different-subdomain")
                .build();

        Tenant tenant3 = Tenant.builder()
                .id("different-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        // Then
        assertEquals(tenant1, tenant2);
        assertNotEquals(tenant1, tenant3);
        assertEquals(tenant1.hashCode(), tenant2.hashCode());
        assertNotEquals(tenant1.hashCode(), tenant3.hashCode());
    }

    @Test
    void testTenantStatusEnum() {
        // Given & When & Then
        assertEquals(4, Tenant.TenantStatus.values().length);
        assertNotNull(Tenant.TenantStatus.valueOf("ACTIVE"));
        assertNotNull(Tenant.TenantStatus.valueOf("INACTIVE"));
        assertNotNull(Tenant.TenantStatus.valueOf("SUSPENDED"));
        assertNotNull(Tenant.TenantStatus.valueOf("PENDING"));
    }

    @Test
    void testTenantToString() {
        // Given
        Tenant tenant = Tenant.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        // When
        String result = tenant.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test Tenant"));
        assertTrue(result.contains("test-tenant"));
    }
} 