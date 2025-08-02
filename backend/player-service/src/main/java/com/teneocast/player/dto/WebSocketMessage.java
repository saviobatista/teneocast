package com.teneocast.player.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {
    
    private String messageId;
    private MessageType type;
    private Map<String, Object> payload;
    private LocalDateTime timestamp;
    private String playerId;
    
    public enum MessageType {
        // Server to Player
        COMMAND,
        UPDATE_SETTINGS,
        HEARTBEAT_PING,
        
        // Player to Server
        STATUS,
        ACK,
        HEARTBEAT_PONG,
        ERROR,
        
        // Bidirectional
        PAIRING_REQUEST,
        PAIRING_RESPONSE
    }
}