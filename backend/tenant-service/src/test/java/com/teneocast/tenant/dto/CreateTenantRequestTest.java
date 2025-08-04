package com.teneocast.tenant.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CreateTenantRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateTenantRequestBuilder() {
        // When
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .preferences("{\"key\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // Then
        assertNotNull(request);
        assertEquals("Test Tenant", request.getName());
        assertEquals("test-tenant", request.getSubdomain());
        assertEquals("{\"key\":\"value\"}", request.getPreferences());
        assertEquals(TenantDto.TenantStatus.ACTIVE, request.getStatus());
    }

    @Test
    void testCreateTenantRequestDefaultValues() {
        // When
        CreateTenantRequest request = new CreateTenantRequest();

        // Then
        assertNull(request.getName());
        assertNull(request.getSubdomain());
        assertNull(request.getPreferences());
        assertNull(request.getStatus());
    }

    @Test
    void testCreateTenantRequestValidationSuccess() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateTenantRequestValidationNameRequired() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testCreateTenantRequestValidationNameTooShort() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("A")
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testCreateTenantRequestValidationNameTooLong() {
        // Given
        String longName = "A".repeat(256);
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name(longName)
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testCreateTenantRequestValidationSubdomainRequired() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testCreateTenantRequestValidationSubdomainTooShort() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("ab")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testCreateTenantRequestValidationSubdomainInvalidCharacters() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test_tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testCreateTenantRequestValidationSubdomainValidCharacters() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant-123")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<CreateTenantRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateTenantRequestEqualsAndHashCode() {
        // Given
        CreateTenantRequest request1 = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .preferences("{\"key\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        CreateTenantRequest request2 = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .preferences("{\"key\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        CreateTenantRequest request3 = CreateTenantRequest.builder()
                .name("Different Tenant")
                .subdomain("test-tenant")
                .preferences("{\"key\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testCreateTenantRequestToString() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .preferences("{\"key\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        String result = request.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test Tenant"));
        assertTrue(result.contains("test-tenant"));
    }

    @Test
    void testCreateTenantRequestJsonSerialization() throws JsonProcessingException {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .preferences("{\"key\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        CreateTenantRequest deserialized = objectMapper.readValue(json, CreateTenantRequest.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("Test Tenant"));
        assertTrue(json.contains("test-tenant"));
        assertEquals(request.getName(), deserialized.getName());
        assertEquals(request.getSubdomain(), deserialized.getSubdomain());
        assertEquals(request.getPreferences(), deserialized.getPreferences());
        assertEquals(request.getStatus(), deserialized.getStatus());
    }
} 