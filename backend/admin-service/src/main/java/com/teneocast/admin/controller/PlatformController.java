package com.teneocast.admin.controller;

import com.teneocast.admin.dto.PlatformSettingsDto;
import com.teneocast.admin.entity.PlatformSettings;
import com.teneocast.admin.service.PlatformSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Slf4j
public class PlatformController {

    private final PlatformSettingsService platformSettingsService;

    @GetMapping
    public ResponseEntity<List<PlatformSettingsDto>> getAllSettings() {
        log.info("Fetching all platform settings");
        List<PlatformSettingsDto> settings = platformSettingsService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/{key}")
    public ResponseEntity<PlatformSettingsDto> getSettingByKey(@PathVariable String key) {
        log.info("Fetching platform setting with key: {}", key);
        return platformSettingsService.getSettingByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PlatformSettingsDto>> getSettingsByType(@PathVariable PlatformSettings.SettingType type) {
        log.info("Fetching platform settings with type: {}", type);
        List<PlatformSettingsDto> settings = platformSettingsService.getSettingsByType(type);
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlatformSettingsDto>> searchSettings(@RequestParam String pattern) {
        log.info("Searching platform settings with pattern: {}", pattern);
        List<PlatformSettingsDto> settings = platformSettingsService.getSettingsByPattern(pattern);
        return ResponseEntity.ok(settings);
    }

    @PostMapping
    public ResponseEntity<PlatformSettingsDto> createSetting(@RequestParam String key,
                                                          @RequestParam String value,
                                                          @RequestParam PlatformSettings.SettingType type,
                                                          @RequestParam(required = false) String description,
                                                          @RequestParam UUID adminUserId) {
        log.info("Creating platform setting with key: {}", key);
        try {
            PlatformSettingsDto createdSetting = platformSettingsService.createSetting(key, value, type, description, adminUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSetting);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create platform setting: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{key}")
    public ResponseEntity<PlatformSettingsDto> updateSetting(@PathVariable String key,
                                                          @RequestParam String value,
                                                          @RequestParam(required = false) String description,
                                                          @RequestParam UUID adminUserId) {
        log.info("Updating platform setting with key: {}", key);
        try {
            PlatformSettingsDto updatedSetting = platformSettingsService.updateSetting(key, value, description, adminUserId);
            return ResponseEntity.ok(updatedSetting);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update platform setting: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteSetting(@PathVariable String key, @RequestParam UUID adminUserId) {
        log.info("Deleting platform setting with key: {}", key);
        try {
            platformSettingsService.deleteSetting(key, adminUserId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete platform setting: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getSettingsCount() {
        log.info("Fetching platform settings count");
        long count = platformSettingsService.getSettingsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PlatformSettingsDto>> getRecentlyUpdatedSettings() {
        log.info("Fetching recently updated platform settings");
        List<PlatformSettingsDto> settings = platformSettingsService.getRecentlyUpdatedSettings();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/updated-by/{adminUserId}")
    public ResponseEntity<List<PlatformSettingsDto>> getSettingsUpdatedBy(@PathVariable UUID adminUserId) {
        log.info("Fetching platform settings updated by admin user: {}", adminUserId);
        List<PlatformSettingsDto> settings = platformSettingsService.getSettingsUpdatedBy(adminUserId);
        return ResponseEntity.ok(settings);
    }
}
