package com.teneocast.player.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatus {
    
    private String playerId;
    private String nowPlaying;
    private Integer volume;
    private Boolean isPlaying;
    private Boolean isMuted;
    private Long uptime;
    private String lastCommand;
    private LocalDateTime lastSeen;
    private Map<String, Object> additionalInfo;
}