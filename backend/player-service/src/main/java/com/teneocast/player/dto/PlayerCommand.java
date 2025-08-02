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
public class PlayerCommand {
    
    private String messageId;
    private CommandType commandType;
    private Map<String, Object> payload;
    private LocalDateTime timestamp;
    private Integer priority;
    
    public enum CommandType {
        PLAY_AD,
        PLAY_TTS,
        PLAY_TRACK,
        PAUSE,
        RESUME,
        SKIP,
        STOP,
        SET_VOLUME,
        UPDATE_SETTINGS,
        SYNC_PLAYLIST,
        HEARTBEAT
    }
}