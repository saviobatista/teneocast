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
class CreateUserRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateUserRequestBuilder() {
        // When
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // Then
        assertNotNull(request);
        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals(TenantUserDto.UserRole.MASTER, request.getRole());
    }

    @Test
    void testCreateUserRequestDefaultValues() {
        // When
        CreateUserRequest request = new CreateUserRequest();

        // Then
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getRole());
    }

    @Test
    void testCreateUserRequestValidationSuccess() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateUserRequestValidationEmailRequired() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testCreateUserRequestValidationEmailInvalid() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("invalid-email")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testCreateUserRequestValidationPasswordRequired() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void testCreateUserRequestValidationPasswordTooShort() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void testCreateUserRequestValidationRoleRequired() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    void testCreateUserRequestEqualsAndHashCode() {
        // Given
        CreateUserRequest request1 = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        CreateUserRequest request2 = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        CreateUserRequest request3 = CreateUserRequest.builder()
                .email("different@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testCreateUserRequestToString() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        String result = request.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test@example.com"));
        assertTrue(result.contains("MASTER"));
    }

    @Test
    void testCreateUserRequestJsonSerialization() throws JsonProcessingException {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        CreateUserRequest deserialized = objectMapper.readValue(json, CreateUserRequest.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("MASTER"));
        assertEquals(request.getEmail(), deserialized.getEmail());
        assertEquals(request.getPassword(), deserialized.getPassword());
        assertEquals(request.getRole(), deserialized.getRole());
    }
} 