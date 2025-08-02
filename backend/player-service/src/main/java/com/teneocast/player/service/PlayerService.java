package com.teneocast.player.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.player.dto.PlayerCommand;
import com.teneocast.player.dto.WebSocketMessage;
import com.teneocast.player.entity.Player;
import com.teneocast.player.entity.PlayerSession;
import com.teneocast.player.repository.PlayerRepository;
import com.teneocast.player.repository.PlayerSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    private final PlayerSessionRepository playerSessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    
    @Value("${player.pairing.code-expiry}")
    private int pairingCodeExpiry;
    
    @Value("${player.pairing.code-length}")
    private int pairingCodeLength;
    
    private static final String PAIRING_CODE_PREFIX = "pair:";
    
    @Transactional
    public Player createPlayer(String tenantId, String name, Player.PlayerPlatform platform, Set<Player.PlayerCapability> capabilities) {
        Player player = Player.builder()
                .name(name)
                .tenantId(tenantId)
                .platform(platform)
                .capabilities(capabilities != null ? capabilities : getDefaultCapabilities())
                .status(Player.PlayerStatus.OFFLINE)
                .isOnline(false)
                .volume(50)
                .build();
        
        return playerRepository.save(player);
    }
    
    @Transactional
    public String generatePairingCode(String playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        
        Player player = playerOpt.get();
        String pairingCode = generateRandomCode();
        LocalDateTime expiry = LocalDateTime.now().plusSeconds(pairingCodeExpiry);
        
        player.setPairingCode(pairingCode);
        player.setPairingCodeExpiry(expiry);
        playerRepository.save(player);
        
        // Store in Redis for fast lookup
        redisTemplate.opsForValue().set(PAIRING_CODE_PREFIX + pairingCode, playerId);
        redisTemplate.expire(PAIRING_CODE_PREFIX + pairingCode, 
                           java.time.Duration.ofSeconds(pairingCodeExpiry));
        
        log.info("Generated pairing code {} for player {}", pairingCode, playerId);
        return pairingCode;
    }
    
    @Transactional
    public String completePairing(String pairingCode, String playerName, String sessionId) {
        // Check Redis first for performance
        String playerId = (String) redisTemplate.opsForValue().get(PAIRING_CODE_PREFIX + pairingCode);
        
        if (playerId == null) {
            // Fallback to database
            Optional<Player> playerOpt = playerRepository.findByPairingCode(pairingCode);
            if (playerOpt.isEmpty() || playerOpt.get().getPairingCodeExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Invalid or expired pairing code: {}", pairingCode);
                return null;
            }
            playerId = playerOpt.get().getId();
        }
        
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            return null;
        }
        
        // Update player name and clear pairing code
        player.setName(playerName != null ? playerName : player.getName());
        player.setPairingCode(null);
        player.setPairingCodeExpiry(null);
        player.setStatus(Player.PlayerStatus.ONLINE);
        player.setIsOnline(true);
        player.setLastSeen(LocalDateTime.now());
        
        playerRepository.save(player);
        
        // Clean up Redis
        redisTemplate.delete(PAIRING_CODE_PREFIX + pairingCode);
        
        log.info("Completed pairing for player {} with session {}", playerId, sessionId);
        return playerId;
    }
    
    public void updatePlayerOnlineStatus(String playerId, boolean isOnline) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setIsOnline(isOnline);
            player.setStatus(isOnline ? Player.PlayerStatus.ONLINE : Player.PlayerStatus.OFFLINE);
            player.setLastSeen(LocalDateTime.now());
            playerRepository.save(player);
            
            log.debug("Updated player {} online status to {}", playerId, isOnline);
        }
    }
    
    public void updatePlayerStatus(String playerId, Map<String, Object> statusData) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            
            // Update fields from status data
            if (statusData.containsKey("nowPlaying")) {
                player.setCurrentTrack((String) statusData.get("nowPlaying"));
            }
            if (statusData.containsKey("volume")) {
                player.setVolume((Integer) statusData.get("volume"));
            }
            if (statusData.containsKey("status")) {
                String status = (String) statusData.get("status");
                try {
                    player.setStatus(Player.PlayerStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid player status: {}", status);
                }
            }
            
            player.setLastSeen(LocalDateTime.now());
            playerRepository.save(player);
            
            log.debug("Updated status for player {}", playerId);
        }
    }
    
    @Transactional
    public PlayerSession createPlayerSession(String playerId, String sessionId, String ipAddress, String userAgent) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        
        PlayerSession session = PlayerSession.builder()
                .player(playerOpt.get())
                .sessionId(sessionId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .connectedAt(LocalDateTime.now())
                .isActive(true)
                .lastPingAt(LocalDateTime.now())
                .build();
        
        return playerSessionRepository.save(session);
    }
    
    @Transactional
    public void closePlayerSession(String sessionId) {
        Optional<PlayerSession> sessionOpt = playerSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            PlayerSession session = sessionOpt.get();
            session.setIsActive(false);
            session.setDisconnectedAt(LocalDateTime.now());
            playerSessionRepository.save(session);
            
            log.debug("Closed session {} for player {}", sessionId, session.getPlayer().getId());
        }
    }
    
    public boolean sendCommandToPlayer(String playerId, PlayerCommand command) {
        Set<WebSocketSession> sessions = sessionManager.getPlayerSessions(playerId);
        if (sessions.isEmpty()) {
            log.warn("No active sessions found for player: {}", playerId);
            return false;
        }
        
        WebSocketMessage message = WebSocketMessage.builder()
                .messageId(command.getMessageId() != null ? command.getMessageId() : UUID.randomUUID().toString())
                .type(WebSocketMessage.MessageType.COMMAND)
                .payload(Map.of(
                    "commandType", command.getCommandType(),
                    "payload", command.getPayload() != null ? command.getPayload() : Map.of(),
                    "priority", command.getPriority() != null ? command.getPriority() : 1
                ))
                .timestamp(LocalDateTime.now())
                .playerId(playerId)
                .build();
        
        boolean sent = false;
        for (WebSocketSession session : sessions) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                sent = true;
                log.debug("Sent command {} to player {} via session {}", 
                         command.getCommandType(), playerId, session.getId());
            } catch (Exception e) {
                log.error("Failed to send command to player {} via session {}", 
                         playerId, session.getId(), e);
            }
        }
        
        return sent;
    }
    
    public List<Player> getPlayersByTenant(String tenantId) {
        return playerRepository.findByTenantId(tenantId);
    }
    
    public Optional<Player> getPlayerById(String playerId) {
        return playerRepository.findById(playerId);
    }
    
    public List<Player> getOnlinePlayersByTenant(String tenantId) {
        return playerRepository.findByTenantIdAndIsOnline(tenantId, true);
    }
    
    public void cleanupExpiredPairingCodes() {
        List<Player> expired = playerRepository.findExpiredPairingCodes(LocalDateTime.now());
        for (Player player : expired) {
            if (player.getPairingCode() != null) {
                redisTemplate.delete(PAIRING_CODE_PREFIX + player.getPairingCode());
                player.setPairingCode(null);
                player.setPairingCodeExpiry(null);
            }
        }
        if (!expired.isEmpty()) {
            playerRepository.saveAll(expired);
            log.info("Cleaned up {} expired pairing codes", expired.size());
        }
    }
    
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < pairingCodeLength; i++) {
            code.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return code.toString();
    }
    
    private Set<Player.PlayerCapability> getDefaultCapabilities() {
        return Set.of(
            Player.PlayerCapability.AUDIO_PLAYBACK,
            Player.PlayerCapability.REMOTE_CONTROL
        );
    }
}