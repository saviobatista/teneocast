package com.teneocast.player.controller;

import com.teneocast.player.entity.Player;
import com.teneocast.player.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

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
    void createPlayer_ShouldReturnCreatedPlayer() {
        // Given
        PlayerController.CreatePlayerRequest request = new PlayerController.CreatePlayerRequest(
                "test-tenant",
                "Test Player",
                Player.PlayerPlatform.WEB,
                Set.of(Player.PlayerCapability.AUDIO_PLAYBACK)
        );
        
        when(playerService.createPlayer(anyString(), anyString(), any(), any()))
                .thenReturn(testPlayer);

        // When
        ResponseEntity<Player> response = playerController.createPlayer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        var player = response.getBody();
        assertThat(player).isNotNull();
        assertThat(player.getId()).isEqualTo("test-player-id");
        assertThat(player.getName()).isEqualTo("Test Player");
        assertThat(player.getTenantId()).isEqualTo("test-tenant");
        assertThat(player.getPlatform()).isEqualTo(Player.PlayerPlatform.WEB);

        verify(playerService).createPlayer("test-tenant", "Test Player", 
                Player.PlayerPlatform.WEB, Set.of(Player.PlayerCapability.AUDIO_PLAYBACK));
    }
} 