package com.teneocast.player.controller;

import com.teneocast.player.dto.PlayerCommand;
import com.teneocast.player.entity.Player;
import com.teneocast.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Slf4j
public class PlayerController {
    
    private final PlayerService playerService;
    
    @PostMapping
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        Player player = playerService.createPlayer(
            request.tenantId(),
            request.name(),
            request.platform(),
            request.capabilities()
        );
        
        log.info("Created player {} for tenant {}", player.getId(), request.tenantId());
        return ResponseEntity.ok(player);
    }
    
    @GetMapping("/{playerId}")
    public ResponseEntity<Player> getPlayer(@PathVariable String playerId) {
        Optional<Player> player = playerService.getPlayerById(playerId);
        return player.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Player>> getPlayersByTenant(@RequestParam String tenantId) {
        List<Player> players = playerService.getPlayersByTenant(tenantId);
        return ResponseEntity.ok(players);
    }
    
    @GetMapping("/online")
    public ResponseEntity<List<Player>> getOnlinePlayersByTenant(@RequestParam String tenantId) {
        List<Player> players = playerService.getOnlinePlayersByTenant(tenantId);
        return ResponseEntity.ok(players);
    }
    
    @PostMapping("/{playerId}/pairing-code")
    public ResponseEntity<Map<String, String>> generatePairingCode(@PathVariable String playerId) {
        try {
            String pairingCode = playerService.generatePairingCode(playerId);
            return ResponseEntity.ok(Map.of("pairingCode", pairingCode));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{playerId}/commands")
    public ResponseEntity<Map<String, Object>> sendCommand(
            @PathVariable String playerId,
            @Valid @RequestBody PlayerCommand command) {
        
        boolean sent = playerService.sendCommandToPlayer(playerId, command);
        
        if (sent) {
            log.info("Command {} sent to player {}", command.getCommandType(), playerId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "messageId", command.getMessageId(),
                "commandType", command.getCommandType()
            ));
        } else {
            log.warn("Failed to send command {} to player {} - no active sessions", 
                    command.getCommandType(), playerId);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Player is not connected"
            ));
        }
    }
    
    @GetMapping("/{playerId}/settings")
    public ResponseEntity<Map<String, Object>> getPlayerSettings(@PathVariable String playerId) {
        Optional<Player> playerOpt = playerService.getPlayerById(playerId);
        
        if (playerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Player player = playerOpt.get();
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("playerId", player.getId());
        settings.put("name", player.getName());
        settings.put("volume", player.getVolume() != null ? player.getVolume() : 50);
        settings.put("platform", player.getPlatform());
        settings.put("capabilities", player.getCapabilities() != null ? player.getCapabilities() : Set.of());
        settings.put("status", player.getStatus());
        settings.put("isOnline", player.getIsOnline() != null ? player.getIsOnline() : false);
        settings.put("lastSeen", player.getLastSeen()); // Can be null
        
        return ResponseEntity.ok(settings);
    }
    
    public record CreatePlayerRequest(
        String tenantId,
        String name,
        Player.PlayerPlatform platform,
        Set<Player.PlayerCapability> capabilities
    ) {}
}