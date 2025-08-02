package com.teneocast.player.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerSessionTest {

    @Test
    void prePersist_ShouldSetDefaultValues() {
        // Given
        Player player = Player.builder().id("player-id").build();
        PlayerSession session = PlayerSession.builder()
                .player(player)
                .sessionId("session-123")
                .build();

        // When
        session.prePersist();

        // Then
        assertThat(session.getIsActive()).isTrue();
        assertThat(session.getConnectedAt()).isNotNull();
        assertThat(session.getConnectedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void prePersist_ShouldNotOverrideExistingValues() {
        // Given
        LocalDateTime customConnectedAt = LocalDateTime.now().minusMinutes(5);
        Player player = Player.builder().id("player-id").build();
        PlayerSession session = PlayerSession.builder()
                .player(player)
                .sessionId("session-123")
                .isActive(false)
                .connectedAt(customConnectedAt)
                .build();

        // When
        session.prePersist();

        // Then
        assertThat(session.getIsActive()).isFalse();
        assertThat(session.getConnectedAt()).isEqualTo(customConnectedAt);
    }

    @Test
    void builder_ShouldCreateSessionWithAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Player player = Player.builder().id("player-id").build();

        // When
        PlayerSession session = PlayerSession.builder()
                .id("session-id")
                .player(player)
                .sessionId("ws-session-123")
                .ipAddress("192.168.1.1")
                .userAgent("TestAgent/1.0")
                .connectedAt(now.minusMinutes(10))
                .disconnectedAt(now.minusMinutes(5))
                .isActive(false)
                .lastPingAt(now.minusMinutes(1))
                .connectionInfo("WebSocket connection")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(session.getId()).isEqualTo("session-id");
        assertThat(session.getPlayer()).isEqualTo(player);
        assertThat(session.getSessionId()).isEqualTo("ws-session-123");
        assertThat(session.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(session.getUserAgent()).isEqualTo("TestAgent/1.0");
        assertThat(session.getConnectedAt()).isEqualTo(now.minusMinutes(10));
        assertThat(session.getDisconnectedAt()).isEqualTo(now.minusMinutes(5));
        assertThat(session.getIsActive()).isFalse();
        assertThat(session.getLastPingAt()).isEqualTo(now.minusMinutes(1));
        assertThat(session.getConnectionInfo()).isEqualTo("WebSocket connection");
        assertThat(session.getCreatedAt()).isEqualTo(now);
        assertThat(session.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toBuilder_ShouldCreateCopyWithModifications() {
        // Given
        Player player = Player.builder().id("player-id").build();
        PlayerSession originalSession = PlayerSession.builder()
                .id("session-id")
                .player(player)
                .sessionId("original-session")
                .isActive(true)
                .ipAddress("127.0.0.1")
                .build();

        // When
        PlayerSession modifiedSession = originalSession.toBuilder()
                .sessionId("modified-session")
                .isActive(false)
                .ipAddress("192.168.1.1")
                .build();

        // Then
        assertThat(modifiedSession.getId()).isEqualTo("session-id");
        assertThat(modifiedSession.getPlayer()).isEqualTo(player);
        assertThat(modifiedSession.getSessionId()).isEqualTo("modified-session");
        assertThat(modifiedSession.getIsActive()).isFalse();
        assertThat(modifiedSession.getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    void equals_ShouldWorkCorrectlyWithSameId() {
        // Given
        Player player = Player.builder().id("player-id").build();
        PlayerSession session1 = PlayerSession.builder().id("same-id").player(player).sessionId("session1").build();
        PlayerSession session2 = PlayerSession.builder().id("same-id").player(player).sessionId("session2").build();

        // When & Then
        assertThat(session1).isEqualTo(session2);
        assertThat(session1.hashCode()).isEqualTo(session2.hashCode());
    }

    @Test
    void equals_ShouldWorkCorrectlyWithDifferentId() {
        // Given
        Player player = Player.builder().id("player-id").build();
        PlayerSession session1 = PlayerSession.builder().id("id-1").player(player).sessionId("session").build();
        PlayerSession session2 = PlayerSession.builder().id("id-2").player(player).sessionId("session").build();

        // When & Then
        assertThat(session1).isNotEqualTo(session2);
    }
}