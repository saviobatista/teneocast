package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.TenantPreferencesDto;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantPreferences;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.repository.TenantPreferencesRepository;
import com.teneocast.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantPreferencesServiceTest {

    @Mock
    private TenantPreferencesRepository tenantPreferencesRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantPreferencesService tenantPreferencesService;

    private Tenant testTenant;
    private TenantPreferences testPreferences;
    private TenantPreferencesDto createRequest;
    private TenantPreferencesDto updateRequest;

    @BeforeEach
    void setUp() {
        testTenant = Tenant.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        testPreferences = TenantPreferences.builder()
                .id(UUID.randomUUID().toString())
                .tenant(testTenant)
                .playbackSettings("{\"volume\":75,\"autoplay\":true}")
                .genrePreferences("[\"rock\",\"jazz\",\"classical\"]")
                .adRules("{\"maxAdsPerHour\":2,\"skipAfter\":30}")
                .volumeDefault(75)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = TenantPreferencesDto.builder()
                .playbackSettings("{\"volume\":80,\"autoplay\":false}")
                .genrePreferences("[\"pop\",\"electronic\"]")
                .adRules("{\"maxAdsPerHour\":1,\"skipAfter\":15}")
                .volumeDefault(80)
                .build();

        updateRequest = TenantPreferencesDto.builder()
                .playbackSettings("{\"volume\":85,\"autoplay\":true}")
                .volumeDefault(85)
                .build();
    }

    @Test
    void testSavePreferences_CreateNew_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());
        when(tenantPreferencesRepository.save(any(TenantPreferences.class))).thenReturn(testPreferences);

        // When
        TenantPreferencesDto result = tenantPreferencesService.savePreferences(testTenant.getId(), createRequest);

        // Then
        assertNotNull(result);
        assertEquals(testPreferences.getId(), result.getId());
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
        verify(tenantPreferencesRepository).save(any(TenantPreferences.class));
    }

    @Test
    void testSavePreferences_UpdateExisting_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testPreferences));
        when(tenantPreferencesRepository.save(any(TenantPreferences.class))).thenReturn(testPreferences);

        // When
        TenantPreferencesDto result = tenantPreferencesService.savePreferences(testTenant.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(testPreferences.getId(), result.getId());
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
        verify(tenantPreferencesRepository).save(any(TenantPreferences.class));
    }

    @Test
    void testSavePreferences_TenantNotFound() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantPreferencesService.savePreferences(testTenant.getId(), createRequest));
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantPreferencesRepository, never()).save(any(TenantPreferences.class));
    }

    @Test
    void testGetPreferences_Success() {
        // Given
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testPreferences));

        // When
        TenantPreferencesDto result = tenantPreferencesService.getPreferences(testTenant.getId());

        // Then
        assertNotNull(result);
        assertEquals(testPreferences.getId(), result.getId());
        assertEquals(testPreferences.getPlaybackSettings(), result.getPlaybackSettings());
        assertEquals(testPreferences.getGenrePreferences(), result.getGenrePreferences());
        assertEquals(testPreferences.getAdRules(), result.getAdRules());
        assertEquals(testPreferences.getVolumeDefault(), result.getVolumeDefault());
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
    }

    @Test
    void testGetPreferences_NotFound() {
        // Given
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantPreferencesService.getPreferences(testTenant.getId()));
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
    }

    @Test
    void testUpdatePreferences_Success() {
        // Given
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testPreferences));
        when(tenantPreferencesRepository.save(any(TenantPreferences.class))).thenReturn(testPreferences);

        // When
        TenantPreferencesDto result = tenantPreferencesService.updatePreferences(testTenant.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(testPreferences.getId(), result.getId());
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
        verify(tenantPreferencesRepository).save(any(TenantPreferences.class));
    }

    @Test
    void testUpdatePreferences_NotFound() {
        // Given
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantPreferencesService.updatePreferences(testTenant.getId(), updateRequest));
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
        verify(tenantPreferencesRepository, never()).save(any(TenantPreferences.class));
    }

    @Test
    void testDeletePreferences_Success() {
        // Given
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testPreferences));

        // When
        tenantPreferencesService.deletePreferences(testTenant.getId());

        // Then
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
        verify(tenantPreferencesRepository).delete(testPreferences);
    }

    @Test
    void testDeletePreferences_NotFound() {
        // Given
        when(tenantPreferencesRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantPreferencesService.deletePreferences(testTenant.getId()));
        verify(tenantPreferencesRepository).findByTenantId(testTenant.getId());
        verify(tenantPreferencesRepository, never()).delete(any(TenantPreferences.class));
    }

    @Test
    void testGetPreferencesByVolumeDefault_Success() {
        // Given
        Integer volumeDefault = 75;
        when(tenantPreferencesRepository.findByVolumeDefault(volumeDefault)).thenReturn(List.of(testPreferences));

        // When
        List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByVolumeDefault(volumeDefault);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPreferences.getId(), result.get(0).getId());
        verify(tenantPreferencesRepository).findByVolumeDefault(volumeDefault);
    }

    @Test
    void testGetPreferencesByVolumeRange_Success() {
        // Given
        Integer minVolume = 70;
        Integer maxVolume = 80;
        when(tenantPreferencesRepository.findByVolumeDefaultBetween(minVolume, maxVolume)).thenReturn(List.of(testPreferences));

        // When
        List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByVolumeRange(minVolume, maxVolume);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPreferences.getId(), result.get(0).getId());
        verify(tenantPreferencesRepository).findByVolumeDefaultBetween(minVolume, maxVolume);
    }

    @Test
    void testGetPreferencesByPlaybackSettings_Success() {
        // Given
        String setting = "volume";
        when(tenantPreferencesRepository.findByPlaybackSettingsContaining(setting)).thenReturn(List.of(testPreferences));

        // When
        List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByPlaybackSettings(setting);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPreferences.getId(), result.get(0).getId());
        verify(tenantPreferencesRepository).findByPlaybackSettingsContaining(setting);
    }

    @Test
    void testGetPreferencesByGenrePreferences_Success() {
        // Given
        String genre = "rock";
        when(tenantPreferencesRepository.findByGenrePreferencesContaining(genre)).thenReturn(List.of(testPreferences));

        // When
        List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByGenrePreferences(genre);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPreferences.getId(), result.get(0).getId());
        verify(tenantPreferencesRepository).findByGenrePreferencesContaining(genre);
    }

    @Test
    void testGetPreferencesByAdRules_Success() {
        // Given
        String rule = "maxAdsPerHour";
        when(tenantPreferencesRepository.findByAdRulesContaining(rule)).thenReturn(List.of(testPreferences));

        // When
        List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByAdRules(rule);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPreferences.getId(), result.get(0).getId());
        verify(tenantPreferencesRepository).findByAdRulesContaining(rule);
    }

    @Test
    void testGetPreferencesCountByVolumeDefault_Success() {
        // Given
        Integer volumeDefault = 75;
        when(tenantPreferencesRepository.countByVolumeDefault(volumeDefault)).thenReturn(5L);

        // When
        long result = tenantPreferencesService.getPreferencesCountByVolumeDefault(volumeDefault);

        // Then
        assertEquals(5L, result);
        verify(tenantPreferencesRepository).countByVolumeDefault(volumeDefault);
    }

    @Test
    void testGetPreferencesCountByVolumeRange_Success() {
        // Given
        Integer minVolume = 70;
        Integer maxVolume = 80;
        when(tenantPreferencesRepository.countByVolumeDefaultBetween(minVolume, maxVolume)).thenReturn(3L);

        // When
        long result = tenantPreferencesService.getPreferencesCountByVolumeRange(minVolume, maxVolume);

        // Then
        assertEquals(3L, result);
        verify(tenantPreferencesRepository).countByVolumeDefaultBetween(minVolume, maxVolume);
    }

    @Test
    void testExistsByTenantId_Success() {
        // Given
        when(tenantPreferencesRepository.existsByTenantId(testTenant.getId())).thenReturn(true);

        // When
        boolean result = tenantPreferencesService.existsByTenantId(testTenant.getId());

        // Then
        assertTrue(result);
        verify(tenantPreferencesRepository).existsByTenantId(testTenant.getId());
    }

    @Test
    void testExistsByTenantId_NotFound() {
        // Given
        when(tenantPreferencesRepository.existsByTenantId(testTenant.getId())).thenReturn(false);

        // When
        boolean result = tenantPreferencesService.existsByTenantId(testTenant.getId());

        // Then
        assertFalse(result);
        verify(tenantPreferencesRepository).existsByTenantId(testTenant.getId());
    }

    @Test
    void testValidatePreferencesRequest_InvalidVolumeDefault() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        TenantPreferencesDto invalidRequest = TenantPreferencesDto.builder()
                .volumeDefault(150) // Invalid: > 100
                .build();

        // When & Then
        assertThrows(TenantValidationException.class, () -> 
            tenantPreferencesService.savePreferences(testTenant.getId(), invalidRequest));
    }

    @Test
    void testValidatePreferencesRequest_NegativeVolumeDefault() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        TenantPreferencesDto invalidRequest = TenantPreferencesDto.builder()
                .volumeDefault(-10) // Invalid: < 0
                .build();

        // When & Then
        assertThrows(TenantValidationException.class, () -> 
            tenantPreferencesService.savePreferences(testTenant.getId(), invalidRequest));
    }
} 