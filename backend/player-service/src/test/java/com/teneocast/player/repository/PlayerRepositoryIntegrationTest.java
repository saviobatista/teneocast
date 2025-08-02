package com.teneocast.player.repository;

import com.teneocast.player.entity.Player;
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
class PlayerRepositoryIntegrationTest {

    @Mock
    private PlayerRepository playerRepository;

    private Player testPlayer1;
    private Player testPlayer2;

    @BeforeEach
    void setUp() {
        testPlayer1 = Player.builder()
                .name("Test Player 1")
                .tenantId("tenant-1")
                .platform(Player.PlayerPlatform.WEB)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK))
                .status(Player.PlayerStatus.OFFLINE)
                .isOnline(false)
                .volume(50)
                .pairingCode("123456")
                .pairingCodeExpiry(LocalDateTime.now().plusMinutes(5))
                .lastSeen(LocalDateTime.now().minusMinutes(1))
                .build();

        testPlayer2 = Player.builder()
                .name("Test Player 2")
                .tenantId("tenant-1")
                .platform(Player.PlayerPlatform.ANDROID)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK, Player.PlayerCapability.TTS_PLAYBACK))
                .status(Player.PlayerStatus.ONLINE)
                .isOnline(true)
                .volume(75)
                .lastSeen(LocalDateTime.now())
                .build();
    }

    @Test
    void findByPairingCode_WhenCodeExists_ShouldReturnPlayer() {
        // Given
        when(playerRepository.findByPairingCode("123456"))
                .thenReturn(Optional.of(testPlayer1));

        // When
        Optional<Player> result = playerRepository.findByPairingCode("123456");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Player 1");
    }
} 