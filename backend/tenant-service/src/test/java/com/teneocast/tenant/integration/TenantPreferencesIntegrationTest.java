package com.teneocast.tenant.integration;

import com.teneocast.tenant.dto.TenantPreferencesDto;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.service.TenantPreferencesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ActiveProfiles("integration-test")
class TenantPreferencesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TenantPreferencesService tenantPreferencesService;

    @Test
    void testSavePreferences_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantPreferencesDto request = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true, \"shuffle\": false}")
                .genrePreferences("[\"pop\", \"rock\", \"jazz\"]")
                .adRules("{\"maxAdsPerHour\": 2, \"skipAfter\": 5}")
                .volumeDefault(75)
                .build();

        // When
        TenantPreferencesDto result = tenantPreferencesService.savePreferences(tenant.getId(), request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenantId()).isEqualTo(tenant.getId());
        assertThat(result.getPlaybackSettings()).isEqualTo("{\"autoPlay\": true, \"shuffle\": false}");
        assertThat(result.getGenrePreferences()).isEqualTo("[\"pop\", \"rock\", \"jazz\"]");
        assertThat(result.getAdRules()).isEqualTo("{\"maxAdsPerHour\": 2, \"skipAfter\": 5}");
        assertThat(result.getVolumeDefault()).isEqualTo(75);
    }

    @Test
    void testGetPreferences_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantPreferencesDto request = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"pop\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(50)
                .build();
        tenantPreferencesService.savePreferences(tenant.getId(), request);

        // When
        TenantPreferencesDto result = tenantPreferencesService.getPreferences(tenant.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenantId()).isEqualTo(tenant.getId());
        assertThat(result.getPlaybackSettings()).isEqualTo("{\"autoPlay\": true}");
        assertThat(result.getGenrePreferences()).isEqualTo("[\"pop\"]");
        assertThat(result.getAdRules()).isEqualTo("{\"maxAdsPerHour\": 1}");
        assertThat(result.getVolumeDefault()).isEqualTo(50);
    }

    @Test
    void testUpdatePreferences_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantPreferencesDto initialRequest = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": false}")
                .genrePreferences("[\"rock\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(30)
                .build();
        tenantPreferencesService.savePreferences(tenant.getId(), initialRequest);

        TenantPreferencesDto updateRequest = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true, \"shuffle\": true}")
                .genrePreferences("[\"pop\", \"jazz\"]")
                .adRules("{\"maxAdsPerHour\": 3, \"skipAfter\": 10}")
                .volumeDefault(80)
                .build();

        // When
        TenantPreferencesDto result = tenantPreferencesService.updatePreferences(tenant.getId(), updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenantId()).isEqualTo(tenant.getId());
        assertThat(result.getPlaybackSettings()).isEqualTo("{\"autoPlay\": true, \"shuffle\": true}");
        assertThat(result.getGenrePreferences()).isEqualTo("[\"pop\", \"jazz\"]");
        assertThat(result.getAdRules()).isEqualTo("{\"maxAdsPerHour\": 3, \"skipAfter\": 10}");
        assertThat(result.getVolumeDefault()).isEqualTo(80);
    }

    @Test
    void testDeletePreferences_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantPreferencesDto request = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"pop\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(50)
                .build();
        tenantPreferencesService.savePreferences(tenant.getId(), request);

        // When & Then
        assertDoesNotThrow(() -> tenantPreferencesService.deletePreferences(tenant.getId()));
    }

    @Test
    void testGetPreferencesByVolumeDefault_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantPreferencesDto request1 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"pop\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(50)
                .build();

        TenantPreferencesDto request2 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": false}")
                .genrePreferences("[\"rock\"]")
                .adRules("{\"maxAdsPerHour\": 2}")
                .volumeDefault(50)
                .build();

        TenantPreferencesDto request3 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"jazz\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(75)
                .build();

        tenantPreferencesService.savePreferences(tenant1.getId(), request1);
        tenantPreferencesService.savePreferences(tenant2.getId(), request2);
        tenantPreferencesService.savePreferences(tenant3.getId(), request3);

        // When
        List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByVolumeDefault(50);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        result.forEach(pref -> assertThat(pref.getVolumeDefault()).isEqualTo(50));
    }

    @Test
    void testGetPreferencesByVolumeRange_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantPreferencesDto request1 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"pop\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(30)
                .build();

        TenantPreferencesDto request2 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": false}")
                .genrePreferences("[\"rock\"]")
                .adRules("{\"maxAdsPerHour\": 2}")
                .volumeDefault(60)
                .build();

        TenantPreferencesDto request3 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"jazz\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(90)
                .build();

        tenantPreferencesService.savePreferences(tenant1.getId(), request1);
        tenantPreferencesService.savePreferences(tenant2.getId(), request2);
        tenantPreferencesService.savePreferences(tenant3.getId(), request3);

        // When
        List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByVolumeRange(40, 70);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVolumeDefault()).isEqualTo(60);
    }

    // @Test
    // void testGetPreferencesByPlaybackSettings_Success() {
    //     // Given
    //     Tenant tenant = createTestTenant();
    //     TenantPreferencesDto request = TenantPreferencesDto.builder()
    //             .playbackSettings("{\"autoPlay\": true, \"shuffle\": true}")
    //             .genrePreferences("[\"pop\"]")
    //             .adRules("{\"maxAdsPerHour\": 1}")
    //             .volumeDefault(50)
    //             .build();
    //     tenantPreferencesService.savePreferences(tenant.getId(), request);

    //     // When
    //     List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByPlaybackSettings("autoPlay");

    //     // Then
    //     assertThat(result).isNotNull();
    //     assertThat(result).hasSize(1);
    //     assertThat(result.get(0).getPlaybackSettings()).contains("autoPlay");
    // }

    // @Test
    // void testGetPreferencesByGenrePreferences_Success() {
    //     // Given
    //     Tenant tenant = createTestTenant();
    //     TenantPreferencesDto request = TenantPreferencesDto.builder()
    //             .playbackSettings("{\"autoPlay\": true}")
    //             .genrePreferences("[\"pop\", \"rock\", \"jazz\"]")
    //             .adRules("{\"maxAdsPerHour\": 1}")
    //             .volumeDefault(50)
    //             .build();
    //     tenantPreferencesService.savePreferences(tenant.getId(), request);

    //     // When
    //     List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByGenrePreferences("pop");

    //     // Then
    //     assertThat(result).isNotNull();
    //     assertThat(result).hasSize(1);
    //     assertThat(result.get(0).getGenrePreferences()).contains("pop");
    // }

    // @Test
    // void testGetPreferencesByAdRules_Success() {
    //     // Given
    //     Tenant tenant = createTestTenant();
    //     TenantPreferencesDto request = TenantPreferencesDto.builder()
    //             .playbackSettings("{\"autoPlay\": true}")
    //             .genrePreferences("[\"pop\"]")
    //             .adRules("{\"maxAdsPerHour\": 2, \"skipAfter\": 5}")
    //             .volumeDefault(50)
    //             .build();
    //     tenantPreferencesService.savePreferences(tenant.getId(), request);

    //     // When
    //     List<TenantPreferencesDto> result = tenantPreferencesService.getPreferencesByAdRules("maxAdsPerHour");

    //     // Then
    //     assertThat(result).isNotNull();
    //     assertThat(result).hasSize(1);
    //     assertThat(result.get(0).getAdRules()).contains("maxAdsPerHour");
    // }

    @Test
    void testGetPreferencesCountByVolumeDefault_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");

        TenantPreferencesDto request1 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"pop\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(50)
                .build();

        TenantPreferencesDto request2 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": false}")
                .genrePreferences("[\"rock\"]")
                .adRules("{\"maxAdsPerHour\": 2}")
                .volumeDefault(50)
                .build();

        tenantPreferencesService.savePreferences(tenant1.getId(), request1);
        tenantPreferencesService.savePreferences(tenant2.getId(), request2);

        // When
        long count = tenantPreferencesService.getPreferencesCountByVolumeDefault(50);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testGetPreferencesCountByVolumeRange_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantPreferencesDto request1 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"pop\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(30)
                .build();

        TenantPreferencesDto request2 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": false}")
                .genrePreferences("[\"rock\"]")
                .adRules("{\"maxAdsPerHour\": 2}")
                .volumeDefault(60)
                .build();

        TenantPreferencesDto request3 = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"jazz\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(90)
                .build();

        tenantPreferencesService.savePreferences(tenant1.getId(), request1);
        tenantPreferencesService.savePreferences(tenant2.getId(), request2);
        tenantPreferencesService.savePreferences(tenant3.getId(), request3);

        // When
        long count = tenantPreferencesService.getPreferencesCountByVolumeRange(40, 70);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testExistsByTenantId_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantPreferencesDto request = TenantPreferencesDto.builder()
                .playbackSettings("{\"autoPlay\": true}")
                .genrePreferences("[\"pop\"]")
                .adRules("{\"maxAdsPerHour\": 1}")
                .volumeDefault(50)
                .build();
        tenantPreferencesService.savePreferences(tenant.getId(), request);

        // When
        boolean exists = tenantPreferencesService.existsByTenantId(tenant.getId());

        // Then
        assertThat(exists).isTrue();
    }
} 