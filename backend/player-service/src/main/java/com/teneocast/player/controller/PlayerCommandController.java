package com.teneocast.player.controller;

import com.teneocast.player.dto.PlayerCommand;
import com.teneocast.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
@Slf4j
public class PlayerCommandController {
    
    private final PlayerService playerService;
    
    @PostMapping("/{playerId}/command")
    public ResponseEntity<Map<String, Object>> sendRemoteCommand(
            @PathVariable String playerId,
            @Valid @RequestBody RemoteCommandRequest request) {
        
        PlayerCommand command = PlayerCommand.builder()
                .messageId(UUID.randomUUID().toString())
                .commandType(request.type())
                .payload(request.payload())
                .timestamp(LocalDateTime.now())
                .priority(request.priority() != null ? request.priority() : 1)
                .build();
        
        boolean sent = playerService.sendCommandToPlayer(playerId, command);
        
        if (sent) {
            log.info("Remote command {} sent to player {}", request.type(), playerId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "messageId", command.getMessageId(),
                "timestamp", command.getTimestamp()
            ));
        } else {
            log.warn("Failed to send remote command {} to player {} - player not connected", 
                    request.type(), playerId);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Player is not connected or not found"
            ));
        }
    }
    
    @PostMapping("/{playerId}/play-ad")
    public ResponseEntity<Map<String, Object>> playAd(
            @PathVariable String playerId,
            @Valid @RequestBody PlayAdRequest request) {
        
        PlayerCommand command = PlayerCommand.builder()
                .messageId(UUID.randomUUID().toString())
                .commandType(PlayerCommand.CommandType.PLAY_AD)
                .payload(Map.of(
                    "adId", request.adId(),
                    "audioUrl", request.audioUrl(),
                    "duration", request.duration() != null ? request.duration() : 30
                ))
                .timestamp(LocalDateTime.now())
                .priority(2) // Ads have higher priority
                .build();
        
        boolean sent = playerService.sendCommandToPlayer(playerId, command);
        
        return sent ? 
            ResponseEntity.ok(Map.of("success", true, "messageId", command.getMessageId())) :
            ResponseEntity.badRequest().body(Map.of("success", false, "error", "Player not connected"));
    }
    
    @PostMapping("/{playerId}/play-tts")
    public ResponseEntity<Map<String, Object>> playTTS(
            @PathVariable String playerId,
            @Valid @RequestBody PlayTTSRequest request) {
        
        PlayerCommand command = PlayerCommand.builder()
                .messageId(UUID.randomUUID().toString())
                .commandType(PlayerCommand.CommandType.PLAY_TTS)
                .payload(Map.of(
                    "text", request.text(),
                    "audioUrl", request.audioUrl() != null ? request.audioUrl() : "",
                    "voice", request.voice() != null ? request.voice() : "default",
                    "priority", request.priority() != null ? request.priority() : 1
                ))
                .timestamp(LocalDateTime.now())
                .priority(request.priority() != null ? request.priority() : 1)
                .build();
        
        boolean sent = playerService.sendCommandToPlayer(playerId, command);
        
        return sent ? 
            ResponseEntity.ok(Map.of("success", true, "messageId", command.getMessageId())) :
            ResponseEntity.badRequest().body(Map.of("success", false, "error", "Player not connected"));
    }
    
    @PostMapping("/{playerId}/pause")
    public ResponseEntity<Map<String, Object>> pausePlayer(@PathVariable String playerId) {
        return sendSimpleCommand(playerId, PlayerCommand.CommandType.PAUSE);
    }
    
    @PostMapping("/{playerId}/resume")
    public ResponseEntity<Map<String, Object>> resumePlayer(@PathVariable String playerId) {
        return sendSimpleCommand(playerId, PlayerCommand.CommandType.RESUME);
    }
    
    @PostMapping("/{playerId}/skip")
    public ResponseEntity<Map<String, Object>> skipTrack(@PathVariable String playerId) {
        return sendSimpleCommand(playerId, PlayerCommand.CommandType.SKIP);
    }
    
    @PostMapping("/{playerId}/stop")
    public ResponseEntity<Map<String, Object>> stopPlayer(@PathVariable String playerId) {
        return sendSimpleCommand(playerId, PlayerCommand.CommandType.STOP);
    }
    
    @PostMapping("/{playerId}/volume")
    public ResponseEntity<Map<String, Object>> setVolume(
            @PathVariable String playerId,
            @Valid @RequestBody SetVolumeRequest request) {
        
        PlayerCommand command = PlayerCommand.builder()
                .messageId(UUID.randomUUID().toString())
                .commandType(PlayerCommand.CommandType.SET_VOLUME)
                .payload(Map.of("volume", request.volume()))
                .timestamp(LocalDateTime.now())
                .priority(1)
                .build();
        
        boolean sent = playerService.sendCommandToPlayer(playerId, command);
        
        return sent ? 
            ResponseEntity.ok(Map.of("success", true, "messageId", command.getMessageId())) :
            ResponseEntity.badRequest().body(Map.of("success", false, "error", "Player not connected"));
    }
    
    private ResponseEntity<Map<String, Object>> sendSimpleCommand(String playerId, PlayerCommand.CommandType commandType) {
        PlayerCommand command = PlayerCommand.builder()
                .messageId(UUID.randomUUID().toString())
                .commandType(commandType)
                .payload(Map.of())
                .timestamp(LocalDateTime.now())
                .priority(1)
                .build();
        
        boolean sent = playerService.sendCommandToPlayer(playerId, command);
        
        return sent ? 
            ResponseEntity.ok(Map.of("success", true, "messageId", command.getMessageId())) :
            ResponseEntity.badRequest().body(Map.of("success", false, "error", "Player not connected"));
    }
    
    public record RemoteCommandRequest(
        PlayerCommand.CommandType type,
        Map<String, Object> payload,
        Integer priority
    ) {}
    
    public record PlayAdRequest(
        String adId,
        String audioUrl,
        Integer duration
    ) {}
    
    public record PlayTTSRequest(
        String text,
        String audioUrl,
        String voice,
        Integer priority
    ) {}
    
    public record SetVolumeRequest(
        Integer volume
    ) {}
}