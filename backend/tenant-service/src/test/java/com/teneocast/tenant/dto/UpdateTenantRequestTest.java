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
class UpdateTenantRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testUpdateTenantRequestBuilder() {
        // When
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .preferences("{\"updated\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // Then
        assertNotNull(request);
        assertEquals("Updated Tenant", request.getName());
        assertEquals("updated-tenant", request.getSubdomain());
        assertEquals("{\"updated\":\"value\"}", request.getPreferences());
        assertEquals(TenantDto.TenantStatus.ACTIVE, request.getStatus());
    }

    @Test
    void testUpdateTenantRequestDefaultValues() {
        // When
        UpdateTenantRequest request = new UpdateTenantRequest();

        // Then
        assertNull(request.getName());
        assertNull(request.getSubdomain());
        assertNull(request.getPreferences());
        assertNull(request.getStatus());
    }

    @Test
    void testUpdateTenantRequestValidationSuccess() {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUpdateTenantRequestValidationNameTooShort() {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("A")
                .subdomain("updated-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testUpdateTenantRequestValidationNameTooLong() {
        // Given
        String longName = "A".repeat(256);
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name(longName)
                .subdomain("updated-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testUpdateTenantRequestValidationSubdomainTooShort() {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("ab")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testUpdateTenantRequestValidationSubdomainInvalidCharacters() {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated_tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testUpdateTenantRequestValidationSubdomainValidCharacters() {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant-123")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUpdateTenantRequestValidationPartialUpdate() {
        // Given - Only name is provided (partial update)
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .build();

        // When
        Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUpdateTenantRequestEqualsAndHashCode() {
        // Given
        UpdateTenantRequest request1 = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .preferences("{\"updated\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        UpdateTenantRequest request2 = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .preferences("{\"updated\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        UpdateTenantRequest request3 = UpdateTenantRequest.builder()
                .name("Different Tenant")
                .subdomain("updated-tenant")
                .preferences("{\"updated\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testUpdateTenantRequestToString() {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .preferences("{\"updated\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        String result = request.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Updated Tenant"));
        assertTrue(result.contains("updated-tenant"));
    }

    @Test
    void testUpdateTenantRequestJsonSerialization() throws JsonProcessingException {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .preferences("{\"updated\":\"value\"}")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        UpdateTenantRequest deserialized = objectMapper.readValue(json, UpdateTenantRequest.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("Updated Tenant"));
        assertTrue(json.contains("updated-tenant"));
        assertEquals(request.getName(), deserialized.getName());
        assertEquals(request.getSubdomain(), deserialized.getSubdomain());
        assertEquals(request.getPreferences(), deserialized.getPreferences());
        assertEquals(request.getStatus(), deserialized.getStatus());
    }
} 