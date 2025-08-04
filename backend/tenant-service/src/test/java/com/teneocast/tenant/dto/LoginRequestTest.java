package com.teneocast.tenant.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLoginRequestBuilder() {
        // Given & When
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("password123")
                .build();

        // Then
        assertEquals("tenant123", request.getTenantId());
        assertEquals("user@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testLoginRequestDefaultValues() {
        // Given & When
        LoginRequest request = new LoginRequest();

        // Then
        assertNull(request.getTenantId());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testLoginRequestAllArgsConstructor() {
        // Given & When
        LoginRequest request = new LoginRequest("tenant123", "user@example.com", "password123");

        // Then
        assertEquals("tenant123", request.getTenantId());
        assertEquals("user@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testLoginRequestValidationSuccess() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLoginRequestValidationTenantIdRequired() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Tenant ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testLoginRequestValidationEmailRequired() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    void testLoginRequestValidationPasswordRequired() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Password is required", violations.iterator().next().getMessage());
    }

    @Test
    void testLoginRequestEqualsAndHashCode() {
        // Given
        LoginRequest request1 = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("password123")
                .build();

        LoginRequest request2 = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("password123")
                .build();

        LoginRequest request3 = LoginRequest.builder()
                .tenantId("tenant456")
                .email("user@example.com")
                .password("password123")
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testLoginRequestToString() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("password123")
                .build();

        // When
        String toString = request.toString();

        // Then
        assertTrue(toString.contains("tenant123"));
        assertTrue(toString.contains("user@example.com"));
        assertTrue(toString.contains("password123"));
    }

    @Test
    void testLoginRequestJsonSerialization() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("password123")
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        LoginRequest deserialized = objectMapper.readValue(json, LoginRequest.class);

        // Then
        assertEquals(request.getTenantId(), deserialized.getTenantId());
        assertEquals(request.getEmail(), deserialized.getEmail());
        assertEquals(request.getPassword(), deserialized.getPassword());
    }

    @Test
    void testLoginRequestJsonDeserialization() throws Exception {
        // Given
        String json = """
                {
                    "tenantId": "tenant123",
                    "email": "user@example.com",
                    "password": "password123"
                }
                """;

        // When
        LoginRequest request = objectMapper.readValue(json, LoginRequest.class);

        // Then
        assertEquals("tenant123", request.getTenantId());
        assertEquals("user@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testLoginRequestSetters() {
        // Given
        LoginRequest request = new LoginRequest();

        // When
        request.setTenantId("tenant123");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        // Then
        assertEquals("tenant123", request.getTenantId());
        assertEquals("user@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }
} 