package com.teneocast.admin.dto;

import com.teneocast.admin.entity.PlatformSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettingsDto {

    private UUID id;
    private String settingKey;
    private String settingValue;
    private PlatformSettings.SettingType settingType;
    private String description;
    private UUID updatedById;
    private String updatedByEmail;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
