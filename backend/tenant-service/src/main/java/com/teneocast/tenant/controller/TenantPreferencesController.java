package com.teneocast.tenant.controller;

import com.teneocast.tenant.dto.TenantPreferencesDto;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantPreferencesService;
import com.teneocast.tenant.service.TenantValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/preferences")
@RequiredArgsConstructor
@Slf4j
public class TenantPreferencesController {

    private final TenantPreferencesService tenantPreferencesService;
    private final TenantValidationService tenantValidationService;

    /**
     * Create or update tenant preferences
     */
    @PostMapping
    public ResponseEntity<TenantPreferencesDto> savePreferences(@PathVariable String tenantId,
                                                             @Valid @RequestBody TenantPreferencesDto request) {
        log.info("Saving preferences for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantPreferencesDto preferences = tenantPreferencesService.savePreferences(tenantId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(preferences);
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found for preferences: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error saving preferences: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get tenant preferences
     */
    @GetMapping
    public ResponseEntity<TenantPreferencesDto> getPreferences(@PathVariable String tenantId) {
        log.debug("Getting preferences for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantPreferencesDto preferences = tenantPreferencesService.getPreferences(tenantId);
            return ResponseEntity.ok(preferences);
        } catch (TenantNotFoundException e) {
            log.warn("Preferences not found for tenant: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Update tenant preferences
     */
    @PutMapping
    public ResponseEntity<TenantPreferencesDto> updatePreferences(@PathVariable String tenantId,
                                                               @Valid @RequestBody TenantPreferencesDto request) {
        log.info("Updating preferences for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantPreferencesDto preferences = tenantPreferencesService.updatePreferences(tenantId, request);
            return ResponseEntity.ok(preferences);
        } catch (TenantNotFoundException e) {
            log.warn("Preferences not found for update: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error updating preferences: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete tenant preferences
     */
    @DeleteMapping
    public ResponseEntity<Void> deletePreferences(@PathVariable String tenantId) {
        log.info("Deleting preferences for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            tenantPreferencesService.deletePreferences(tenantId);
            return ResponseEntity.noContent().build();
        } catch (TenantNotFoundException e) {
            log.warn("Preferences not found for deletion: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format for deletion: {}", tenantId);
            throw e;
        }
    }

    /**
     * Get preferences by volume default
     */
    @GetMapping("/volume/{volumeDefault}")
    public ResponseEntity<List<TenantPreferencesDto>> getPreferencesByVolumeDefault(@PathVariable Integer volumeDefault) {
        log.debug("Getting preferences by volume default: {}", volumeDefault);
        
        List<TenantPreferencesDto> preferences = tenantPreferencesService.getPreferencesByVolumeDefault(volumeDefault);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get preferences by volume range
     */
    @GetMapping("/volume/range")
    public ResponseEntity<List<TenantPreferencesDto>> getPreferencesByVolumeRange(
            @RequestParam Integer minVolume,
            @RequestParam Integer maxVolume) {
        log.debug("Getting preferences by volume range: {} - {}", minVolume, maxVolume);
        
        List<TenantPreferencesDto> preferences = tenantPreferencesService.getPreferencesByVolumeRange(minVolume, maxVolume);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get preferences containing specific playback settings
     */
    @GetMapping("/playback-settings")
    public ResponseEntity<List<TenantPreferencesDto>> getPreferencesByPlaybackSettings(@RequestParam String setting) {
        log.debug("Getting preferences by playback settings: {}", setting);
        
        if (setting == null || setting.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<TenantPreferencesDto> preferences = tenantPreferencesService.getPreferencesByPlaybackSettings(setting.trim());
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get preferences containing specific genre preferences
     */
    @GetMapping("/genre-preferences")
    public ResponseEntity<List<TenantPreferencesDto>> getPreferencesByGenrePreferences(@RequestParam String genre) {
        log.debug("Getting preferences by genre preferences: {}", genre);
        
        if (genre == null || genre.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<TenantPreferencesDto> preferences = tenantPreferencesService.getPreferencesByGenrePreferences(genre.trim());
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get preferences containing specific ad rules
     */
    @GetMapping("/ad-rules")
    public ResponseEntity<List<TenantPreferencesDto>> getPreferencesByAdRules(@RequestParam String rule) {
        log.debug("Getting preferences by ad rules: {}", rule);
        
        if (rule == null || rule.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<TenantPreferencesDto> preferences = tenantPreferencesService.getPreferencesByAdRules(rule.trim());
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get preferences count by volume default
     */
    @GetMapping("/count/volume/{volumeDefault}")
    public ResponseEntity<Long> getPreferencesCountByVolumeDefault(@PathVariable Integer volumeDefault) {
        log.debug("Getting preferences count by volume default: {}", volumeDefault);
        
        long count = tenantPreferencesService.getPreferencesCountByVolumeDefault(volumeDefault);
        return ResponseEntity.ok(count);
    }

    /**
     * Get preferences count by volume range
     */
    @GetMapping("/count/volume/range")
    public ResponseEntity<Long> getPreferencesCountByVolumeRange(
            @RequestParam Integer minVolume,
            @RequestParam Integer maxVolume) {
        log.debug("Getting preferences count by volume range: {} - {}", minVolume, maxVolume);
        
        long count = tenantPreferencesService.getPreferencesCountByVolumeRange(minVolume, maxVolume);
        return ResponseEntity.ok(count);
    }

    /**
     * Check if preferences exist for tenant
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByTenantId(@PathVariable String tenantId) {
        log.debug("Checking if preferences exist for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            boolean exists = tenantPreferencesService.existsByTenantId(tenantId);
            return ResponseEntity.ok(exists);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health(@PathVariable String tenantId) {
        log.debug("Health check endpoint called for tenant preferences: {}", tenantId);
        return ResponseEntity.ok("Tenant preferences service is healthy for tenant: " + tenantId);
    }
} 