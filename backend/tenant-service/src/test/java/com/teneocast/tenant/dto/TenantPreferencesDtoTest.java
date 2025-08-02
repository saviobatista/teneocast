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
class TenantPreferencesDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testTenantPreferencesDtoBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        TenantPreferencesDto dto = TenantPreferencesDto.builder()
                .id("pref-id")
                .tenantId("tenant-id")
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals("pref-id", dto.getId());
        assertEquals("tenant-id", dto.getTenantId());
        assertEquals("{\"volume\":50}", dto.getPlaybackSettings());
        assertEquals("[\"pop\",\"rock\"]", dto.getGenrePreferences());
        assertEquals("{\"interval\":5}", dto.getAdRules());
        assertEquals(75, dto.getVolumeDefault());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testTenantPreferencesDtoDefaultValues() {
        // When
        TenantPreferencesDto dto = new TenantPreferencesDto();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getTenantId());
        assertNull(dto.getPlaybackSettings());
        assertNull(dto.getGenrePreferences());
        assertNull(dto.getAdRules());
        assertNull(dto.getVolumeDefault());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testTenantPreferencesDtoEqualsAndHashCode() {
        // Given
        TenantPreferencesDto dto1 = TenantPreferencesDto.builder()
                .id("pref-id")
                .tenantId("tenant-id")
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        TenantPreferencesDto dto2 = TenantPreferencesDto.builder()
                .id("pref-id")
                .tenantId("tenant-id")
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        TenantPreferencesDto dto3 = TenantPreferencesDto.builder()
                .id("different-id")
                .tenantId("tenant-id")
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testTenantPreferencesDtoToString() {
        // Given
        TenantPreferencesDto dto = TenantPreferencesDto.builder()
                .id("pref-id")
                .tenantId("tenant-id")
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("pref-id"));
        assertTrue(result.contains("75"));
    }

    @Test
    void testTenantPreferencesDtoJsonSerialization() throws JsonProcessingException {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TenantPreferencesDto dto = TenantPreferencesDto.builder()
                .id("pref-id")
                .tenantId("tenant-id")
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        TenantPreferencesDto deserialized = objectMapper.readValue(json, TenantPreferencesDto.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("pref-id"));
        assertTrue(json.contains("75"));
        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getTenantId(), deserialized.getTenantId());
        assertEquals(dto.getPlaybackSettings(), deserialized.getPlaybackSettings());
        assertEquals(dto.getGenrePreferences(), deserialized.getGenrePreferences());
        assertEquals(dto.getAdRules(), deserialized.getAdRules());
        assertEquals(dto.getVolumeDefault(), deserialized.getVolumeDefault());
    }
} 