package com.teneocast.player.controller;

import com.teneocast.player.service.WebSocketSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private WebSocketSessionManager sessionManager;

    @InjectMocks
    private HealthController healthController;

    @Test
    void health_ShouldReturnHealthStatus() {
        // Given
        when(sessionManager.getAllActivePlayers()).thenReturn(Set.of("player1", "player2"));
        when(sessionManager.getTotalActiveSessions()).thenReturn(3);

        // When
        var response = healthController.health();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("UP");
        assertThat(body.get("service")).isEqualTo("player-service");
        assertThat(body.get("activePlayers")).isEqualTo(2);
        assertThat(body.get("activeSessions")).isEqualTo(3);
        assertThat(body.get("timestamp")).isNotNull();
    }
}