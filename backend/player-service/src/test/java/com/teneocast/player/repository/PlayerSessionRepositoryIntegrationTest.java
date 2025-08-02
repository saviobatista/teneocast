package com.teneocast.player.repository;

import com.teneocast.player.entity.Player;
import com.teneocast.player.entity.PlayerSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerSessionRepositoryIntegrationTest {

    @Mock
    private PlayerSessionRepository playerSessionRepository;

    private Player testPlayer;
    private PlayerSession activeSession;
    private PlayerSession inactiveSession;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .name("Test Player")
                .tenantId("tenant-1")
                .platform(Player.PlayerPlatform.WEB)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK))
                .status(Player.PlayerStatus.ONLINE)
                .isOnline(true)
                .volume(50)
                .build();

        activeSession = PlayerSession.builder()
                .player(testPlayer)
                .sessionId("active-session-123")
                .ipAddress("127.0.0.1")
                .userAgent("test-agent/1.0")
                .connectedAt(LocalDateTime.now().minusMinutes(10))
                .isActive(true)
                .lastPingAt(LocalDateTime.now().minusMinutes(1))
                .build();

        inactiveSession = PlayerSession.builder()
                .player(testPlayer)
                .sessionId("inactive-session-456")
                .ipAddress("127.0.0.1")
                .userAgent("test-agent/1.0")
                .connectedAt(LocalDateTime.now().minusHours(1))
                .disconnectedAt(LocalDateTime.now().minusMinutes(30))
                .isActive(false)
                .lastPingAt(LocalDateTime.now().minusMinutes(30))
                .build();
    }

    @Test
    void findBySessionId_WhenSessionExists_ShouldReturnSession() {
        // Given
        when(playerSessionRepository.findBySessionId("active-session-123"))
                .thenReturn(Optional.of(activeSession));

        // When
        Optional<PlayerSession> result = playerSessionRepository.findBySessionId("active-session-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPlayer()).isEqualTo(testPlayer);
        assertThat(result.get().getIpAddress()).isEqualTo("127.0.0.1");
    }
} 