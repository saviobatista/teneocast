package com.teneocast.player.controller;

import com.teneocast.player.dto.PlayerCommand;
import com.teneocast.player.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerCommandControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerCommandController playerCommandController;

    @Test
    void sendRemoteCommand_WhenPlayerConnected_ShouldReturnSuccess() {
        // Given
        PlayerCommandController.RemoteCommandRequest request = 
                new PlayerCommandController.RemoteCommandRequest(
                        PlayerCommand.CommandType.PAUSE,
                        Map.of(),
                        1
                );
        
        when(playerService.sendCommandToPlayer(eq("test-player"), any(PlayerCommand.class)))
                .thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = 
                playerCommandController.sendRemoteCommand("test-player", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(true);
        assertThat(body.get("messageId")).isNotNull();
        assertThat(body.get("timestamp")).isNotNull();

        verify(playerService).sendCommandToPlayer(eq("test-player"), any(PlayerCommand.class));
    }
} 