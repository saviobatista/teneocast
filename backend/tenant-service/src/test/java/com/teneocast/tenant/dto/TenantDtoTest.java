package com.teneocast.tenant.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
class TenantDtoTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testTenantDtoBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        TenantDto dto = TenantDto.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .preferences("{\"key\":\"value\"}")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals("test-id", dto.getId());
        assertEquals("Test Tenant", dto.getName());
        assertEquals("test-tenant", dto.getSubdomain());
        assertEquals(TenantDto.TenantStatus.ACTIVE, dto.getStatus());
        assertEquals("{\"key\":\"value\"}", dto.getPreferences());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testTenantDtoDefaultValues() {
        // When
        TenantDto dto = new TenantDto();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getSubdomain());
        assertNull(dto.getStatus());
        assertNull(dto.getPreferences());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testTenantDtoValidationSuccess() {
        // Given
        TenantDto dto = TenantDto.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantDtoValidationNameRequired() {
        // Given
        TenantDto dto = TenantDto.builder()
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testTenantDtoValidationNameTooShort() {
        // Given
        TenantDto dto = TenantDto.builder()
                .name("A")
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testTenantDtoValidationNameTooLong() {
        // Given
        String longName = "A".repeat(256);
        TenantDto dto = TenantDto.builder()
                .name(longName)
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testTenantDtoValidationSubdomainRequired() {
        // Given
        TenantDto dto = TenantDto.builder()
                .name("Test Tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testTenantDtoValidationSubdomainTooShort() {
        // Given
        TenantDto dto = TenantDto.builder()
                .name("Test Tenant")
                .subdomain("ab")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testTenantDtoValidationSubdomainInvalidCharacters() {
        // Given
        TenantDto dto = TenantDto.builder()
                .name("Test Tenant")
                .subdomain("test_tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("subdomain")));
    }

    @Test
    void testTenantDtoValidationSubdomainValidCharacters() {
        // Given
        TenantDto dto = TenantDto.builder()
                .name("Test Tenant")
                .subdomain("test-tenant-123")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<TenantDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantDtoEqualsAndHashCode() {
        // Given
        TenantDto dto1 = TenantDto.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        TenantDto dto2 = TenantDto.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        TenantDto dto3 = TenantDto.builder()
                .id("different-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testTenantDtoStatusEnum() {
        // Given & When & Then
        assertEquals(4, TenantDto.TenantStatus.values().length);
        assertNotNull(TenantDto.TenantStatus.valueOf("ACTIVE"));
        assertNotNull(TenantDto.TenantStatus.valueOf("INACTIVE"));
        assertNotNull(TenantDto.TenantStatus.valueOf("SUSPENDED"));
        assertNotNull(TenantDto.TenantStatus.valueOf("PENDING"));
    }

    @Test
    void testTenantDtoToString() {
        // Given
        TenantDto dto = TenantDto.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test Tenant"));
        assertTrue(result.contains("test-tenant"));
    }

    @Test
    void testTenantDtoJsonSerialization() throws JsonProcessingException {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TenantDto dto = TenantDto.builder()
                .id("test-id")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .preferences("{\"key\":\"value\"}")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        TenantDto deserialized = objectMapper.readValue(json, TenantDto.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("Test Tenant"));
        assertTrue(json.contains("test-tenant"));
        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getName(), deserialized.getName());
        assertEquals(dto.getSubdomain(), deserialized.getSubdomain());
        assertEquals(dto.getStatus(), deserialized.getStatus());
    }
} 