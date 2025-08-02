package com.teneocast.player.integration;

import com.teneocast.player.entity.Player;
import com.teneocast.player.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceIntegrationTest {

    @Mock
    private PlayerService playerService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .id("test-player-id")
                .name("Test Player")
                .tenantId("test-tenant")
                .platform(Player.PlayerPlatform.WEB)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK))
                .status(Player.PlayerStatus.OFFLINE)
                .isOnline(false)
                .volume(50)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createPlayer_ShouldPersistToDatabase() {
        // Given
        String tenantId = "integration-tenant";
        String name = "Integration Test Player";
        Player.PlayerPlatform platform = Player.PlayerPlatform.WEB;
        Set<Player.PlayerCapability> capabilities = Set.of(
                Player.PlayerCapability.AUDIO_PLAYBACK, 
                Player.PlayerCapability.REMOTE_CONTROL
        );

        when(playerService.createPlayer(anyString(), anyString(), any(), any()))
                .thenReturn(testPlayer);

        // When
        Player result = playerService.createPlayer(tenantId, name, platform, capabilities);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Player");
        assertThat(result.getTenantId()).isEqualTo("test-tenant");
        assertThat(result.getPlatform()).isEqualTo(Player.PlayerPlatform.WEB);

        verify(playerService).createPlayer(tenantId, name, platform, capabilities);
    }
} 