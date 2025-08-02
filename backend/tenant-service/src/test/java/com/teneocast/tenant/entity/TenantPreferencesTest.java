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
class TenantPreferencesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testTenantPreferencesBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        
        // When
        TenantPreferences preferences = TenantPreferences.builder()
                .id("pref-id")
                .tenant(tenant)
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(preferences);
        assertEquals("pref-id", preferences.getId());
        assertEquals(tenant, preferences.getTenant());
        assertEquals("{\"volume\":50}", preferences.getPlaybackSettings());
        assertEquals("[\"pop\",\"rock\"]", preferences.getGenrePreferences());
        assertEquals("{\"interval\":5}", preferences.getAdRules());
        assertEquals(75, preferences.getVolumeDefault());
        assertEquals(now, preferences.getCreatedAt());
        assertEquals(now, preferences.getUpdatedAt());
    }

    @Test
    void testTenantPreferencesDefaultValues() {
        // When
        TenantPreferences preferences = new TenantPreferences();

        // Then
        assertNull(preferences.getId());
        assertNull(preferences.getTenant());
        assertEquals("{}", preferences.getPlaybackSettings());
        assertEquals("[]", preferences.getGenrePreferences());
        assertEquals("{}", preferences.getAdRules());
        assertEquals(50, preferences.getVolumeDefault());
        assertNull(preferences.getCreatedAt());
        assertNull(preferences.getUpdatedAt());
    }

    @Test
    void testTenantPreferencesPrePersist() {
        // Given
        TenantPreferences preferences = new TenantPreferences();
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        preferences.setTenant(tenant);

        // When
        preferences.prePersist();

        // Then
        assertEquals("{}", preferences.getPlaybackSettings());
        assertEquals("[]", preferences.getGenrePreferences());
        assertEquals("{}", preferences.getAdRules());
        assertEquals(50, preferences.getVolumeDefault());
        assertNotNull(preferences.getCreatedAt());
        assertNotNull(preferences.getUpdatedAt());
    }

    @Test
    void testTenantPreferencesPrePersistWithNullValues() {
        // Given
        TenantPreferences preferences = new TenantPreferences();
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        preferences.setTenant(tenant);
        preferences.setPlaybackSettings(null);
        preferences.setGenrePreferences(null);
        preferences.setAdRules(null);
        preferences.setVolumeDefault(null);

        // When
        preferences.prePersist();

        // Then
        assertEquals("{}", preferences.getPlaybackSettings());
        assertEquals("[]", preferences.getGenrePreferences());
        assertEquals("{}", preferences.getAdRules());
        assertEquals(50, preferences.getVolumeDefault());
        assertNotNull(preferences.getCreatedAt());
        assertNotNull(preferences.getUpdatedAt());
    }

    @Test
    void testTenantPreferencesPreUpdate() {
        // Given
        TenantPreferences preferences = TenantPreferences.builder()
                .tenant(Tenant.builder().id("tenant-id").build())
                .build();
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(1);
        preferences.setUpdatedAt(originalUpdatedAt);

        // When
        preferences.preUpdate();

        // Then
        assertTrue(preferences.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testTenantPreferencesValidationSuccess() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantPreferences preferences = TenantPreferences.builder()
                .tenant(tenant)
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        // When
        Set<ConstraintViolation<TenantPreferences>> violations = validator.validate(preferences);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantPreferencesValidationTenantRequired() {
        // Given
        TenantPreferences preferences = TenantPreferences.builder()
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        // When
        Set<ConstraintViolation<TenantPreferences>> violations = validator.validate(preferences);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tenant")));
    }

    @Test
    void testTenantPreferencesEqualsAndHashCode() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantPreferences preferences1 = TenantPreferences.builder()
                .id("pref-id")
                .tenant(tenant)
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        TenantPreferences preferences2 = TenantPreferences.builder()
                .id("pref-id")
                .tenant(tenant)
                .playbackSettings("{\"different\":\"settings\"}")
                .genrePreferences("[\"jazz\",\"classical\"]")
                .adRules("{\"different\":\"rules\"}")
                .volumeDefault(100)
                .build();

        TenantPreferences preferences3 = TenantPreferences.builder()
                .id("different-id")
                .tenant(tenant)
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        // Then
        assertEquals(preferences1, preferences2);
        assertNotEquals(preferences1, preferences3);
        assertEquals(preferences1.hashCode(), preferences2.hashCode());
        assertNotEquals(preferences1.hashCode(), preferences3.hashCode());
    }

    @Test
    void testTenantPreferencesToString() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantPreferences preferences = TenantPreferences.builder()
                .id("pref-id")
                .tenant(tenant)
                .playbackSettings("{\"volume\":50}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"interval\":5}")
                .volumeDefault(75)
                .build();

        // When
        String result = preferences.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("pref-id"));
        assertTrue(result.contains("75"));
    }
} 