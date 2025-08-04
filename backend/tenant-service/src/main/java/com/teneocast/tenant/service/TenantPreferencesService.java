package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.TenantPreferencesDto;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantPreferences;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.repository.TenantPreferencesRepository;
import com.teneocast.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantPreferencesService {

    private final TenantPreferencesRepository tenantPreferencesRepository;
    private final TenantRepository tenantRepository;
    private final TenantValidationService tenantValidationService;

    /**
     * Create or update tenant preferences
     */
    public TenantPreferencesDto savePreferences(String tenantId, TenantPreferencesDto request) {
        log.info("Saving preferences for tenant: {}", tenantId);
        
        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + tenantId));
        
        // Validate request
        validatePreferencesRequest(request);
        
        // Check if preferences already exist
        Optional<TenantPreferences> existingPrefs = tenantPreferencesRepository.findByTenantId(tenantId);
        
        TenantPreferences preferences;
        if (existingPrefs.isPresent()) {
            // Update existing preferences
            preferences = existingPrefs.get();
            updatePreferences(preferences, request);
        } else {
            // Create new preferences
            preferences = createPreferences(tenant, request);
        }
        
        preferences.setUpdatedAt(LocalDateTime.now());
        TenantPreferences savedPrefs = tenantPreferencesRepository.save(preferences);
        
        log.info("Saved preferences for tenant: {}", tenantId);
        return mapToDto(savedPrefs);
    }

    /**
     * Get tenant preferences
     */
    @Transactional(readOnly = true)
    public TenantPreferencesDto getPreferences(String tenantId) {
        log.debug("Getting preferences for tenant: {}", tenantId);
        
        TenantPreferences preferences = tenantPreferencesRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Preferences not found for tenant: " + tenantId));
        
        return mapToDto(preferences);
    }

    /**
     * Update specific preference fields
     */
    public TenantPreferencesDto updatePreferences(String tenantId, TenantPreferencesDto request) {
        log.info("Updating preferences for tenant: {}", tenantId);
        
        TenantPreferences preferences = tenantPreferencesRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Preferences not found for tenant: " + tenantId));
        
        // Validate request
        validatePreferencesRequest(request);
        
        // Update fields
        updatePreferences(preferences, request);
        preferences.setUpdatedAt(LocalDateTime.now());
        
        TenantPreferences savedPrefs = tenantPreferencesRepository.save(preferences);
        log.info("Updated preferences for tenant: {}", tenantId);
        
        return mapToDto(savedPrefs);
    }

    /**
     * Delete tenant preferences
     */
    public void deletePreferences(String tenantId) {
        log.info("Deleting preferences for tenant: {}", tenantId);
        
        TenantPreferences preferences = tenantPreferencesRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Preferences not found for tenant: " + tenantId));
        
        tenantPreferencesRepository.delete(preferences);
        log.info("Deleted preferences for tenant: {}", tenantId);
    }

    /**
     * Get preferences by volume default
     */
    @Transactional(readOnly = true)
    public List<TenantPreferencesDto> getPreferencesByVolumeDefault(Integer volumeDefault) {
        log.debug("Getting preferences by volume default: {}", volumeDefault);
        
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByVolumeDefault(volumeDefault);
        return preferences.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get preferences by volume range
     */
    @Transactional(readOnly = true)
    public List<TenantPreferencesDto> getPreferencesByVolumeRange(Integer minVolume, Integer maxVolume) {
        log.debug("Getting preferences by volume range: {} - {}", minVolume, maxVolume);
        
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByVolumeDefaultBetween(minVolume, maxVolume);
        return preferences.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get preferences containing specific playback settings
     */
    @Transactional(readOnly = true)
    public List<TenantPreferencesDto> getPreferencesByPlaybackSettings(String setting) {
        log.debug("Getting preferences by playback settings: {}", setting);
        
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByPlaybackSettingsContaining(setting);
        return preferences.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get preferences containing specific genre preferences
     */
    @Transactional(readOnly = true)
    public List<TenantPreferencesDto> getPreferencesByGenrePreferences(String genre) {
        log.debug("Getting preferences by genre preferences: {}", genre);
        
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByGenrePreferencesContaining(genre);
        return preferences.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get preferences containing specific ad rules
     */
    @Transactional(readOnly = true)
    public List<TenantPreferencesDto> getPreferencesByAdRules(String rule) {
        log.debug("Getting preferences by ad rules: {}", rule);
        
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByAdRulesContaining(rule);
        return preferences.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Get preferences count by volume default
     */
    @Transactional(readOnly = true)
    public long getPreferencesCountByVolumeDefault(Integer volumeDefault) {
        log.debug("Getting preferences count by volume default: {}", volumeDefault);
        return tenantPreferencesRepository.countByVolumeDefault(volumeDefault);
    }

    /**
     * Get preferences count by volume range
     */
    @Transactional(readOnly = true)
    public long getPreferencesCountByVolumeRange(Integer minVolume, Integer maxVolume) {
        log.debug("Getting preferences count by volume range: {} - {}", minVolume, maxVolume);
        return tenantPreferencesRepository.countByVolumeDefaultBetween(minVolume, maxVolume);
    }

    /**
     * Check if preferences exist for tenant
     */
    @Transactional(readOnly = true)
    public boolean existsByTenantId(String tenantId) {
        return tenantPreferencesRepository.existsByTenantId(tenantId);
    }

    /**
     * Create new preferences
     */
    private TenantPreferences createPreferences(Tenant tenant, TenantPreferencesDto request) {
        return TenantPreferences.builder()
                .tenant(tenant)
                .playbackSettings(request.getPlaybackSettings() != null ? request.getPlaybackSettings() : "{}")
                .genrePreferences(request.getGenrePreferences() != null ? request.getGenrePreferences() : "[]")
                .adRules(request.getAdRules() != null ? request.getAdRules() : "{}")
                .volumeDefault(request.getVolumeDefault() != null ? request.getVolumeDefault() : 50)
                .build();
    }

    /**
     * Update existing preferences
     */
    private void updatePreferences(TenantPreferences preferences, TenantPreferencesDto request) {
        if (request.getPlaybackSettings() != null) {
            preferences.setPlaybackSettings(request.getPlaybackSettings());
        }
        if (request.getGenrePreferences() != null) {
            preferences.setGenrePreferences(request.getGenrePreferences());
        }
        if (request.getAdRules() != null) {
            preferences.setAdRules(request.getAdRules());
        }
        if (request.getVolumeDefault() != null) {
            preferences.setVolumeDefault(request.getVolumeDefault());
        }
    }

    /**
     * Validate preferences request
     */
    private void validatePreferencesRequest(TenantPreferencesDto request) {
        if (request == null) {
            throw new TenantValidationException("Preferences request cannot be null");
        }
        
        if (request.getVolumeDefault() != null) {
            if (request.getVolumeDefault() < 0 || request.getVolumeDefault() > 100) {
                throw new TenantValidationException("Volume default must be between 0 and 100");
            }
        }
        
        if (request.getPlaybackSettings() != null) {
            validateJsonString(request.getPlaybackSettings(), "Playback settings");
        }
        
        if (request.getGenrePreferences() != null) {
            validateJsonString(request.getGenrePreferences(), "Genre preferences");
        }
        
        if (request.getAdRules() != null) {
            validateJsonString(request.getAdRules(), "Ad rules");
        }
    }

    /**
     * Validate JSON string
     */
    private void validateJsonString(String jsonString, String fieldName) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return;
        }
        
        try {
            // Basic JSON validation - check if it starts and ends with proper brackets/braces
            String trimmed = jsonString.trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                throw new TenantValidationException(fieldName + " must be valid JSON");
            }
            if (!trimmed.endsWith("}") && !trimmed.endsWith("]")) {
                throw new TenantValidationException(fieldName + " must be valid JSON");
            }
        } catch (Exception e) {
            throw new TenantValidationException(fieldName + " must be valid JSON: " + e.getMessage());
        }
    }

    /**
     * Map entity to DTO
     */
    private TenantPreferencesDto mapToDto(TenantPreferences preferences) {
        return TenantPreferencesDto.builder()
                .id(preferences.getId())
                .tenantId(preferences.getTenant().getId())
                .playbackSettings(preferences.getPlaybackSettings())
                .genrePreferences(preferences.getGenrePreferences())
                .adRules(preferences.getAdRules())
                .volumeDefault(preferences.getVolumeDefault())
                .createdAt(preferences.getCreatedAt())
                .updatedAt(preferences.getUpdatedAt())
                .build();
    }
} 