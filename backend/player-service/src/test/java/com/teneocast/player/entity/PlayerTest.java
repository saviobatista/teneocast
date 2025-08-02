package com.teneocast.player.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerTest {

    @Test
    void prePersist_ShouldSetDefaultValues() {
        // Given
        Player player = Player.builder()
                .name("Test Player")
                .tenantId("tenant-1")
                .platform(Player.PlayerPlatform.WEB)
                .build();

        // When
        player.prePersist();

        // Then
        assertThat(player.getStatus()).isEqualTo(Player.PlayerStatus.OFFLINE);
        assertThat(player.getIsOnline()).isFalse();
        assertThat(player.getVolume()).isEqualTo(50);
    }

    @Test
    void prePersist_ShouldNotOverrideExistingValues() {
        // Given
        Player player = Player.builder()
                .name("Test Player")
                .tenantId("tenant-1")
                .platform(Player.PlayerPlatform.WEB)
                .status(Player.PlayerStatus.ONLINE)
                .isOnline(true)
                .volume(75)
                .build();

        // When
        player.prePersist();

        // Then
        assertThat(player.getStatus()).isEqualTo(Player.PlayerStatus.ONLINE);
        assertThat(player.getIsOnline()).isTrue();
        assertThat(player.getVolume()).isEqualTo(75);
    }

    @Test
    void builder_ShouldCreatePlayerWithAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<Player.PlayerCapability> capabilities = Set.of(
                Player.PlayerCapability.AUDIO_PLAYBACK,
                Player.PlayerCapability.TTS_PLAYBACK
        );

        // When
        Player player = Player.builder()
                .id("test-id")
                .name("Test Player")
                .tenantId("tenant-1")
                .pairingCode("123456")
                .pairingCodeExpiry(now.plusMinutes(5))
                .status(Player.PlayerStatus.PLAYING)
                .platform(Player.PlayerPlatform.ANDROID)
                .deviceInfo("Android 12")
                .appVersion("1.0.0")
                .lastSeen(now)
                .currentTrack("track-123")
                .volume(80)
                .isOnline(true)
                .capabilities(capabilities)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(player.getId()).isEqualTo("test-id");
        assertThat(player.getName()).isEqualTo("Test Player");
        assertThat(player.getTenantId()).isEqualTo("tenant-1");
        assertThat(player.getPairingCode()).isEqualTo("123456");
        assertThat(player.getPairingCodeExpiry()).isEqualTo(now.plusMinutes(5));
        assertThat(player.getStatus()).isEqualTo(Player.PlayerStatus.PLAYING);
        assertThat(player.getPlatform()).isEqualTo(Player.PlayerPlatform.ANDROID);
        assertThat(player.getDeviceInfo()).isEqualTo("Android 12");
        assertThat(player.getAppVersion()).isEqualTo("1.0.0");
        assertThat(player.getLastSeen()).isEqualTo(now);
        assertThat(player.getCurrentTrack()).isEqualTo("track-123");
        assertThat(player.getVolume()).isEqualTo(80);
        assertThat(player.getIsOnline()).isTrue();
        assertThat(player.getCapabilities()).isEqualTo(capabilities);
        assertThat(player.getCreatedAt()).isEqualTo(now);
        assertThat(player.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toBuilder_ShouldCreateCopyWithModifications() {
        // Given
        Player originalPlayer = Player.builder()
                .id("test-id")
                .name("Original Name")
                .tenantId("tenant-1")
                .platform(Player.PlayerPlatform.WEB)
                .status(Player.PlayerStatus.OFFLINE)
                .volume(50)
                .isOnline(false)
                .build();

        // When
        Player modifiedPlayer = originalPlayer.toBuilder()
                .name("Modified Name")
                .status(Player.PlayerStatus.ONLINE)
                .isOnline(true)
                .build();

        // Then
        assertThat(modifiedPlayer.getId()).isEqualTo("test-id");
        assertThat(modifiedPlayer.getName()).isEqualTo("Modified Name");
        assertThat(modifiedPlayer.getTenantId()).isEqualTo("tenant-1");
        assertThat(modifiedPlayer.getPlatform()).isEqualTo(Player.PlayerPlatform.WEB);
        assertThat(modifiedPlayer.getStatus()).isEqualTo(Player.PlayerStatus.ONLINE);
        assertThat(modifiedPlayer.getVolume()).isEqualTo(50);
        assertThat(modifiedPlayer.getIsOnline()).isTrue();
    }

    @Test
    void playerStatus_ShouldHaveAllExpectedValues() {
        // Then
        assertThat(Player.PlayerStatus.values()).containsExactlyInAnyOrder(
                Player.PlayerStatus.OFFLINE,
                Player.PlayerStatus.ONLINE,
                Player.PlayerStatus.PLAYING,
                Player.PlayerStatus.PAUSED,
                Player.PlayerStatus.BUFFERING,
                Player.PlayerStatus.ERROR
        );
    }

    @Test
    void playerPlatform_ShouldHaveAllExpectedValues() {
        // Then
        assertThat(Player.PlayerPlatform.values()).containsExactlyInAnyOrder(
                Player.PlayerPlatform.WEB,
                Player.PlayerPlatform.WINDOWS,
                Player.PlayerPlatform.ANDROID,
                Player.PlayerPlatform.IOS
        );
    }

    @Test
    void playerCapability_ShouldHaveAllExpectedValues() {
        // Then
        assertThat(Player.PlayerCapability.values()).containsExactlyInAnyOrder(
                Player.PlayerCapability.AUDIO_PLAYBACK,
                Player.PlayerCapability.TTS_PLAYBACK,
                Player.PlayerCapability.REMOTE_CONTROL,
                Player.PlayerCapability.OFFLINE_MODE,
                Player.PlayerCapability.BACKGROUND_PLAYBACK
        );
    }

    @Test
    void equals_ShouldWorkCorrectlyWithSameId() {
        // Given
        Player player1 = Player.builder().id("same-id").name("Player 1").build();
        Player player2 = Player.builder().id("same-id").name("Player 2").build();

        // When & Then
        assertThat(player1).isEqualTo(player2);
        assertThat(player1.hashCode()).isEqualTo(player2.hashCode());
    }

    @Test
    void equals_ShouldWorkCorrectlyWithDifferentId() {
        // Given
        Player player1 = Player.builder().id("id-1").name("Player 1").build();
        Player player2 = Player.builder().id("id-2").name("Player 1").build();

        // When & Then
        assertThat(player1).isNotEqualTo(player2);
    }
}