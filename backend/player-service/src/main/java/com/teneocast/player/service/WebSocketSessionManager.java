package com.teneocast.player.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketSessionManager {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Local session storage - sessions are not shared across instances
    private final ConcurrentMap<String, ConcurrentMap<String, WebSocketSession>> playerSessions = new ConcurrentHashMap<>();
    
    private static final String PLAYER_LAST_SEEN_KEY = "player:lastSeen:";
    private static final String PLAYER_SESSIONS_KEY = "player:sessions:";
    
    public void addSession(String playerId, WebSocketSession session) {
        playerSessions.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                     .put(session.getId(), session);
        
        // Store session info in Redis for cross-instance awareness
        redisTemplate.opsForSet().add(PLAYER_SESSIONS_KEY + playerId, session.getId());
        updateLastSeen(playerId);
        
        log.info("Added WebSocket session {} for player {}", session.getId(), playerId);
    }
    
    public void removeSession(String playerId, String sessionId) {
        ConcurrentMap<String, WebSocketSession> sessions = playerSessions.get(playerId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                playerSessions.remove(playerId);
            }
        }
        
        // Remove from Redis
        redisTemplate.opsForSet().remove(PLAYER_SESSIONS_KEY + playerId, sessionId);
        
        log.info("Removed WebSocket session {} for player {}", sessionId, playerId);
    }
    
    public boolean hasActiveSessions(String playerId) {
        // Check both local and Redis for cross-instance awareness
        ConcurrentMap<String, WebSocketSession> localSessions = playerSessions.get(playerId);
        boolean hasLocalSessions = localSessions != null && !localSessions.isEmpty();
        
        Set<Object> redisSessions = redisTemplate.opsForSet().members(PLAYER_SESSIONS_KEY + playerId);
        boolean hasRedisSessions = redisSessions != null && !redisSessions.isEmpty();
        
        return hasLocalSessions || hasRedisSessions;
    }
    
    public Set<WebSocketSession> getPlayerSessions(String playerId) {
        ConcurrentMap<String, WebSocketSession> sessions = playerSessions.get(playerId);
        return sessions != null ? Set.copyOf(sessions.values()) : Set.of();
    }
    
    public void updateLastSeen(String playerId) {
        redisTemplate.opsForValue().set(PLAYER_LAST_SEEN_KEY + playerId, LocalDateTime.now().toString());
    }
    
    public LocalDateTime getLastSeen(String playerId) {
        String lastSeenStr = (String) redisTemplate.opsForValue().get(PLAYER_LAST_SEEN_KEY + playerId);
        return lastSeenStr != null ? LocalDateTime.parse(lastSeenStr) : null;
    }
    
    public void cleanupStaleSessionInfo(String playerId) {
        redisTemplate.delete(PLAYER_SESSIONS_KEY + playerId);
        redisTemplate.delete(PLAYER_LAST_SEEN_KEY + playerId);
    }
    
    public Set<String> getAllActivePlayers() {
        return playerSessions.keySet();
    }
    
    public int getTotalActiveSessions() {
        return playerSessions.values().stream()
                            .mapToInt(ConcurrentMap::size)
                            .sum();
    }
}