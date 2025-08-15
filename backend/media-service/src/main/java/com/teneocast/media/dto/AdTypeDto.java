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
public class AdTypeDto {
    private Long id;
    private UUID tenantId;
    private String name;
    private String description;
    private Boolean isSelectable;
    private Boolean canPlayRemotely;
    private Boolean canPlayIndividually;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
