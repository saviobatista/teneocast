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
class UpdateUserRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testUpdateUserRequestBuilder() {
        // When
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        // Then
        assertNotNull(request);
        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals(TenantUserDto.UserRole.MASTER, request.getRole());
        assertTrue(request.getIsActive());
    }

    @Test
    void testUpdateUserRequestDefaultValues() {
        // When
        UpdateUserRequest request = new UpdateUserRequest();

        // Then
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getRole());
        assertNull(request.getIsActive());
    }

    @Test
    void testUpdateUserRequestValidationSuccess() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        // When
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUpdateUserRequestValidationEmailInvalid() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("invalid-email")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testUpdateUserRequestValidationPasswordTooShort() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("test@example.com")
                .password("123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void testUpdateUserRequestValidationOptionalFields() {
        // Given - All fields are optional in UpdateUserRequest
        UpdateUserRequest request = UpdateUserRequest.builder().build();

        // When
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUpdateUserRequestEqualsAndHashCode() {
        // Given
        UpdateUserRequest request1 = UpdateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        UpdateUserRequest request2 = UpdateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        UpdateUserRequest request3 = UpdateUserRequest.builder()
                .email("different@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testUpdateUserRequestToString() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        // When
        String result = request.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test@example.com"));
        assertTrue(result.contains("MASTER"));
    }

    @Test
    void testUpdateUserRequestJsonSerialization() throws JsonProcessingException {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        UpdateUserRequest deserialized = objectMapper.readValue(json, UpdateUserRequest.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("MASTER"));
        assertEquals(request.getEmail(), deserialized.getEmail());
        assertEquals(request.getPassword(), deserialized.getPassword());
        assertEquals(request.getRole(), deserialized.getRole());
        assertEquals(request.getIsActive(), deserialized.getIsActive());
    }
} 