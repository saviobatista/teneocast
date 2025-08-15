package com.teneocast.media.dto;

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
public class AdvertisementDto {
    private Long id;
    private UUID tenantId;
    private Long adTypeId;
    private String adTypeName;
    private String name;
    private String description;
    private Integer durationSeconds;
    private String filePath;
    private Long fileSize;
    private String fileFormat;
    private String targetAudience;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
