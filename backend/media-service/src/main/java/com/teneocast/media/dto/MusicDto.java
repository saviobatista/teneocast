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
public class MusicDto {
    private Long id;
    private UUID tenantId;
    private Long genreId;
    private String genreName;
    private String title;
    private String artist;
    private String album;
    private Integer durationSeconds;
    private String filePath;
    private Long fileSize;
    private String fileFormat;
    private Integer bitrate;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
