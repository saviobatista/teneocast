package com.teneocast.tenant.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TenantUserDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testTenantUserDtoBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        TenantUserDto dto = TenantUserDto.builder()
                .id("user-id")
                .tenantId("tenant-id")
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals("user-id", dto.getId());
        assertEquals("tenant-id", dto.getTenantId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
        assertEquals(TenantUserDto.UserRole.MASTER, dto.getRole());
        assertTrue(dto.getIsActive());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals(now, dto.getLastLoginAt());
    }

    @Test
    void testTenantUserDtoDefaultValues() {
        // When
        TenantUserDto dto = new TenantUserDto();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getTenantId());
        assertNull(dto.getEmail());
        assertNull(dto.getPassword());
        assertNull(dto.getRole());
        assertNull(dto.getIsActive());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertNull(dto.getLastLoginAt());
    }

    @Test
    void testTenantUserDtoEqualsAndHashCode() {
        // Given
        TenantUserDto dto1 = TenantUserDto.builder()
                .id("user-id")
                .tenantId("tenant-id")
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        TenantUserDto dto2 = TenantUserDto.builder()
                .id("user-id")
                .tenantId("tenant-id")
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        TenantUserDto dto3 = TenantUserDto.builder()
                .id("different-id")
                .tenantId("tenant-id")
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testTenantUserDtoRoleEnum() {
        // Given & When & Then
        assertEquals(3, TenantUserDto.UserRole.values().length);
        assertNotNull(TenantUserDto.UserRole.valueOf("MASTER"));
        assertNotNull(TenantUserDto.UserRole.valueOf("PRODUCER"));
        assertNotNull(TenantUserDto.UserRole.valueOf("MANAGER"));
    }

    @Test
    void testTenantUserDtoToString() {
        // Given
        TenantUserDto dto = TenantUserDto.builder()
                .id("user-id")
                .tenantId("tenant-id")
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test@example.com"));
        assertTrue(result.contains("MASTER"));
    }

    @Test
    void testTenantUserDtoJsonSerialization() throws JsonProcessingException {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TenantUserDto dto = TenantUserDto.builder()
                .id("user-id")
                .tenantId("tenant-id")
                .email("test@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        TenantUserDto deserialized = objectMapper.readValue(json, TenantUserDto.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("MASTER"));
        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getTenantId(), deserialized.getTenantId());
        assertEquals(dto.getEmail(), deserialized.getEmail());
        assertEquals(dto.getRole(), deserialized.getRole());
    }
} 