package com.teneocast.player.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.WebSocketSession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebSocketSessionManagerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private WebSocketSession webSocketSession1;

    @Mock
    private WebSocketSession webSocketSession2;

    @InjectMocks
    private WebSocketSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(webSocketSession1.getId()).thenReturn("session-1");
        when(webSocketSession2.getId()).thenReturn("session-2");
    }

    @Test
    void addSession_ShouldAddSessionToLocalAndRedis() {
        // When
        sessionManager.addSession("player-1", webSocketSession1);

        // Then
        verify(setOperations).add("player:sessions:player-1", "session-1");
        verify(valueOperations).set(eq("player:lastSeen:player-1"), anyString());
        
        // Verify local storage
        Set<WebSocketSession> sessions = sessionManager.getPlayerSessions("player-1");
        assertThat(sessions).containsExactly(webSocketSession1);
    }

    @Test
    void removeSession_ShouldRemoveSessionFromLocalAndRedis() {
        // Given
        sessionManager.addSession("player-1", webSocketSession1);
        sessionManager.addSession("player-1", webSocketSession2);

        // When
        sessionManager.removeSession("player-1", "session-1");

        // Then
        verify(setOperations).remove("player:sessions:player-1", "session-1");
        
        // Verify local storage
        Set<WebSocketSession> sessions = sessionManager.getPlayerSessions("player-1");
        assertThat(sessions).containsExactly(webSocketSession2);
    }

    @Test
    void removeSession_WhenLastSession_ShouldRemovePlayerFromLocal() {
        // Given
        sessionManager.addSession("player-1", webSocketSession1);

        // When
        sessionManager.removeSession("player-1", "session-1");

        // Then
        Set<WebSocketSession> sessions = sessionManager.getPlayerSessions("player-1");
        assertThat(sessions).isEmpty();
    }

    @Test
    void hasActiveSessions_WithLocalSessions_ShouldReturnTrue() {
        // Given
        sessionManager.addSession("player-1", webSocketSession1);

        // When
        boolean result = sessionManager.hasActiveSessions("player-1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasActiveSessions_WithRedisSessions_ShouldReturnTrue() {
        // Given
        when(setOperations.members("player:sessions:player-1"))
                .thenReturn(Set.of("session-1"));

        // When
        boolean result = sessionManager.hasActiveSessions("player-1");

        // Then
        assertThat(result).isTrue();
        verify(setOperations).members("player:sessions:player-1");
    }

    @Test
    void hasActiveSessions_WithNoSessions_ShouldReturnFalse() {
        // Given
        when(setOperations.members("player:sessions:player-1"))
                .thenReturn(Set.of());

        // When
        boolean result = sessionManager.hasActiveSessions("player-1");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getPlayerSessions_ShouldReturnLocalSessions() {
        // Given
        sessionManager.addSession("player-1", webSocketSession1);
        sessionManager.addSession("player-1", webSocketSession2);

        // When
        Set<WebSocketSession> result = sessionManager.getPlayerSessions("player-1");

        // Then
        assertThat(result).containsExactlyInAnyOrder(webSocketSession1, webSocketSession2);
    }

    @Test
    void getPlayerSessions_WhenNoSessions_ShouldReturnEmptySet() {
        // When
        Set<WebSocketSession> result = sessionManager.getPlayerSessions("player-1");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void updateLastSeen_ShouldUpdateRedis() {
        // When
        sessionManager.updateLastSeen("player-1");

        // Then
        verify(valueOperations).set(eq("player:lastSeen:player-1"), anyString());
    }

    @Test
    void getLastSeen_WhenExists_ShouldReturnDateTime() {
        // Given
        String timestamp = "2023-12-01T10:00:00";
        when(valueOperations.get("player:lastSeen:player-1")).thenReturn(timestamp);

        // When
        LocalDateTime result = sessionManager.getLastSeen("player-1");

        // Then
        assertThat(result).isEqualTo(LocalDateTime.parse(timestamp));
        verify(valueOperations).get("player:lastSeen:player-1");
    }

    @Test
    void getLastSeen_WhenNotExists_ShouldReturnNull() {
        // Given
        when(valueOperations.get("player:lastSeen:player-1")).thenReturn(null);

        // When
        LocalDateTime result = sessionManager.getLastSeen("player-1");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void cleanupStaleSessionInfo_ShouldDeleteRedisKeys() {
        // When
        sessionManager.cleanupStaleSessionInfo("player-1");

        // Then
        verify(redisTemplate).delete("player:sessions:player-1");
        verify(redisTemplate).delete("player:lastSeen:player-1");
    }

    @Test
    void getAllActivePlayers_ShouldReturnActivePlayerIds() {
        // Given
        sessionManager.addSession("player-1", webSocketSession1);
        sessionManager.addSession("player-2", webSocketSession2);

        // When
        Set<String> result = sessionManager.getAllActivePlayers();

        // Then
        assertThat(result).containsExactlyInAnyOrder("player-1", "player-2");
    }

    @Test
    void getTotalActiveSessions_ShouldReturnTotalCount() {
        // Given
        sessionManager.addSession("player-1", webSocketSession1);
        sessionManager.addSession("player-1", webSocketSession2);
        
        WebSocketSession session3 = mock(WebSocketSession.class);
        when(session3.getId()).thenReturn("session-3");
        sessionManager.addSession("player-2", session3);

        // When
        int result = sessionManager.getTotalActiveSessions();

        // Then
        assertThat(result).isEqualTo(3);
    }

    @Test
    void getTotalActiveSessions_WhenNoSessions_ShouldReturnZero() {
        // When
        int result = sessionManager.getTotalActiveSessions();

        // Then
        assertThat(result).isEqualTo(0);
    }
}