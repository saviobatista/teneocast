package com.teneocast.admin.service;

import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.entity.PlatformSettings;
import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.admin.repository.PlatformSettingsRepository;
import com.teneocast.common.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformSettingsServiceTest {

    @Mock
    private PlatformSettingsRepository platformSettingsRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    private PlatformSettingsService platformSettingsService;

    @BeforeEach
    void setUp() {
        platformSettingsService = new PlatformSettingsService(platformSettingsRepository, adminUserRepository);
    }

    @Test
    void platformSettingsService_ShouldBeInstantiated() {
        assertNotNull(platformSettingsService);
    }

    @Test
    void createSetting_ShouldCreateSuccessfully() {
        // Given
        String settingKey = "test.setting";
        String settingValue = "test_value";
        PlatformSettings.SettingType settingType = PlatformSettings.SettingType.STRING;
        String description = "Test setting";
        UUID adminUserId = UUID.randomUUID();

        AdminUser adminUser = AdminUser.builder()
                .id(adminUserId)
                .email("admin@teneocast.com")
                .role(UserRole.ROOT)
                .build();

        PlatformSettings savedSetting = PlatformSettings.builder()
                .id(UUID.randomUUID())
                .settingKey(settingKey)
                .settingValue(settingValue)
                .settingType(settingType)
                .description(description)
                .updatedBy(adminUser)
                .build();

        when(platformSettingsRepository.existsBySettingKey(settingKey)).thenReturn(false);
        when(adminUserRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(platformSettingsRepository.save(any(PlatformSettings.class))).thenReturn(savedSetting);

        // When
        var result = platformSettingsService.createSetting(settingKey, settingValue, settingType, description, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(settingKey, result.getSettingKey());
        assertEquals(settingValue, result.getSettingValue());
        assertEquals(settingType, result.getSettingType());

        verify(platformSettingsRepository).existsBySettingKey(settingKey);
        verify(adminUserRepository).findById(adminUserId);
        verify(platformSettingsRepository).save(any(PlatformSettings.class));
    }

    @Test
    void createSetting_ShouldThrowException_WhenKeyExists() {
        // Given
        String settingKey = "existing.setting";
        UUID adminUserId = UUID.randomUUID();

        when(platformSettingsRepository.existsBySettingKey(settingKey)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            platformSettingsService.createSetting(settingKey, "value", PlatformSettings.SettingType.STRING, "desc", adminUserId));
        
        verify(platformSettingsRepository).existsBySettingKey(settingKey);
        verify(adminUserRepository, never()).findById(any());
        verify(platformSettingsRepository, never()).save(any());
    }
}
