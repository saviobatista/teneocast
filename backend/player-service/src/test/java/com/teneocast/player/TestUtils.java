package com.teneocast.player;

import com.teneocast.player.entity.Player;
import com.teneocast.player.entity.PlayerSession;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Utility class for creating test objects with common configurations.
 */
public class TestUtils {

    public static Player createTestPlayer() {
        return createTestPlayer("test-player", "test-tenant");
    }

    public static Player createTestPlayer(String name, String tenantId) {
        return Player.builder()
                .name(name)
                .tenantId(tenantId)
                .platform(Player.PlayerPlatform.WEB)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK))
                .status(Player.PlayerStatus.OFFLINE)
                .isOnline(false)
                .volume(50)
                .build();
    }

    public static Player createOnlinePlayer(String name, String tenantId) {
        return Player.builder()
                .name(name)
                .tenantId(tenantId)
                .platform(Player.PlayerPlatform.WEB)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK, Player.PlayerCapability.REMOTE_CONTROL))
                .status(Player.PlayerStatus.ONLINE)
                .isOnline(true)
                .volume(75)
                .lastSeen(LocalDateTime.now())
                .build();
    }

    public static Player createPlayerWithPairingCode(String name, String tenantId, String pairingCode) {
        return Player.builder()
                .name(name)
                .tenantId(tenantId)
                .platform(Player.PlayerPlatform.ANDROID)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK))
                .status(Player.PlayerStatus.OFFLINE)
                .isOnline(false)
                .volume(50)
                .pairingCode(pairingCode)
                .pairingCodeExpiry(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    public static PlayerSession createTestSession(Player player, String sessionId) {
        return PlayerSession.builder()
                .player(player)
                .sessionId(sessionId)
                .ipAddress("127.0.0.1")
                .userAgent("test-agent/1.0")
                .connectedAt(LocalDateTime.now().minusMinutes(10))
                .isActive(true)
                .lastPingAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    public static PlayerSession createInactiveSession(Player player, String sessionId) {
        return PlayerSession.builder()
                .player(player)
                .sessionId(sessionId)
                .ipAddress("127.0.0.1")
                .userAgent("test-agent/1.0")
                .connectedAt(LocalDateTime.now().minusHours(1))
                .disconnectedAt(LocalDateTime.now().minusMinutes(30))
                .isActive(false)
                .lastPingAt(LocalDateTime.now().minusMinutes(30))
                .build();
    }
}