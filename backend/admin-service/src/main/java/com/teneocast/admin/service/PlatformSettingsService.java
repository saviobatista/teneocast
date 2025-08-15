package com.teneocast.admin.service;

import com.teneocast.admin.dto.PlatformSettingsDto;
import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.entity.PlatformSettings;
import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.admin.repository.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatformSettingsService {

    private final PlatformSettingsRepository platformSettingsRepository;
    private final AdminUserRepository adminUserRepository;

    public List<PlatformSettingsDto> getAllSettings() {
        return platformSettingsRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<PlatformSettingsDto> getSettingByKey(String settingKey) {
        return platformSettingsRepository.findBySettingKey(settingKey)
                .map(this::mapToDto);
    }

    public List<PlatformSettingsDto> getSettingsByType(PlatformSettings.SettingType settingType) {
        return platformSettingsRepository.findBySettingType(settingType)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PlatformSettingsDto> getSettingsByPattern(String pattern) {
        return platformSettingsRepository.findBySettingKeyPattern(pattern)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PlatformSettingsDto createSetting(String settingKey, String settingValue, 
                                          PlatformSettings.SettingType settingType, 
                                          String description, UUID adminUserId) {
        
        if (platformSettingsRepository.existsBySettingKey(settingKey)) {
            throw new IllegalArgumentException("Setting with key '" + settingKey + "' already exists");
        }

        AdminUser adminUser = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found with id: " + adminUserId));

        PlatformSettings setting = PlatformSettings.builder()
                .settingKey(settingKey)
                .settingValue(settingValue)
                .settingType(settingType)
                .description(description)
                .updatedBy(adminUser)
                .build();

        PlatformSettings savedSetting = platformSettingsRepository.save(setting);
        log.info("Created platform setting: {} by admin: {}", settingKey, adminUser.getEmail());
        
        return mapToDto(savedSetting);
    }

    public PlatformSettingsDto updateSetting(String settingKey, String settingValue, 
                                          String description, UUID adminUserId) {
        
        PlatformSettings setting = platformSettingsRepository.findBySettingKey(settingKey)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with key: " + settingKey));

        AdminUser adminUser = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found with id: " + adminUserId));

        setting.setSettingValue(settingValue);
        if (description != null) {
            setting.setDescription(description);
        }
        setting.setUpdatedBy(adminUser);
        setting.setUpdatedAt(LocalDateTime.now());

        PlatformSettings savedSetting = platformSettingsRepository.save(setting);
        log.info("Updated platform setting: {} by admin: {}", settingKey, adminUser.getEmail());
        
        return mapToDto(savedSetting);
    }

    public void deleteSetting(String settingKey, UUID adminUserId) {
        PlatformSettings setting = platformSettingsRepository.findBySettingKey(settingKey)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with key: " + settingKey));

        AdminUser adminUser = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found with id: " + adminUserId));

        platformSettingsRepository.delete(setting);
        log.info("Deleted platform setting: {} by admin: {}", settingKey, adminUser.getEmail());
    }

    public long getSettingsCount() {
        return platformSettingsRepository.countSettings();
    }

    public List<PlatformSettingsDto> getRecentlyUpdatedSettings() {
        return platformSettingsRepository.findRecentlyUpdatedSettings()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PlatformSettingsDto> getSettingsUpdatedBy(UUID adminUserId) {
        return platformSettingsRepository.findByUpdatedBy(adminUserId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PlatformSettingsDto mapToDto(PlatformSettings setting) {
        return PlatformSettingsDto.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .settingType(setting.getSettingType())
                .description(setting.getDescription())
                .updatedById(setting.getUpdatedBy() != null ? setting.getUpdatedBy().getId() : null)
                .updatedByEmail(setting.getUpdatedBy() != null ? setting.getUpdatedBy().getEmail() : null)
                .updatedAt(setting.getUpdatedAt())
                .createdAt(setting.getCreatedAt())
                .build();
    }
}
