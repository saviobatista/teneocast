package com.teneocast.player.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.player.dto.WebSocketMessage;
import com.teneocast.player.service.PlayerService;
import com.teneocast.player.service.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerWebSocketHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PlayerService playerService;

    @Mock
    private WebSocketSessionManager sessionManager;

    @Mock
    private WebSocketSession webSocketSession;

    @InjectMocks
    private PlayerWebSocketHandler webSocketHandler;

    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        sessionAttributes.put("playerId", "test-player");
        sessionAttributes.put("ipAddress", "127.0.0.1");
        sessionAttributes.put("userAgent", "test-agent");
        
        when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
        when(webSocketSession.getId()).thenReturn("session-123");
        
        // Mock objectMapper to return a valid JSON string
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"type\":\"CONNECTED\",\"payload\":{\"status\":\"connected\"}}");
        } catch (Exception e) {
            // Ignore exception in test setup
        }
    }

    @Test
    void afterConnectionEstablished_ShouldSetupSession() throws Exception {
        // When
        webSocketHandler.afterConnectionEstablished(webSocketSession);

        // Then
        verify(sessionManager).addSession("test-player", webSocketSession);
        verify(playerService).updatePlayerOnlineStatus("test-player", true);
        verify(playerService).createPlayerSession("test-player", "session-123", "127.0.0.1", "test-agent");
        verify(objectMapper).writeValueAsString(any(WebSocketMessage.class));
        verify(webSocketSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void afterConnectionEstablished_WhenExceptionOccurs_ShouldCloseSession() throws Exception {
        // Given
        doThrow(new RuntimeException("Test exception")).when(sessionManager).addSession(anyString(), any());

        // When
        webSocketHandler.afterConnectionEstablished(webSocketSession);

        // Then
        verify(webSocketSession).close();
    }

    @Test
    void handleTextMessage_WithStatusMessage_ShouldUpdatePlayerStatus() throws Exception {
        // Given
        WebSocketMessage statusMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.STATUS)
                .payload(Map.of("nowPlaying", "test-track"))
                .build();
        
        TextMessage textMessage = new TextMessage("{\"type\":\"STATUS\"}");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenReturn(statusMessage);

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(sessionManager).updateLastSeen("test-player");
        verify(playerService).updatePlayerStatus("test-player", Map.of("nowPlaying", "test-track"));
    }

    @Test
    void handleTextMessage_WithAckMessage_ShouldLogAcknowledgment() throws Exception {
        // Given
        WebSocketMessage ackMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.ACK)
                .payload(Map.of("messageId", "msg-123"))
                .build();
        
        TextMessage textMessage = new TextMessage("{\"type\":\"ACK\"}");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenReturn(ackMessage);

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(sessionManager).updateLastSeen("test-player");
        // ACK messages are just logged, no further processing needed
    }

    @Test
    void handleTextMessage_WithHeartbeatPong_ShouldUpdateLastSeen() throws Exception {
        // Given
        WebSocketMessage pongMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.HEARTBEAT_PONG)
                .payload(Map.of())
                .build();
        
        TextMessage textMessage = new TextMessage("{\"type\":\"HEARTBEAT_PONG\"}");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenReturn(pongMessage);

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(sessionManager, times(2)).updateLastSeen("test-player"); // Once in main handler, once in pong handler
    }

    @Test
    void handleTextMessage_WithErrorMessage_ShouldLogError() throws Exception {
        // Given
        WebSocketMessage errorMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.ERROR)
                .payload(Map.of("error", "Test error"))
                .build();
        
        TextMessage textMessage = new TextMessage("{\"type\":\"ERROR\"}");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenReturn(errorMessage);

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(sessionManager).updateLastSeen("test-player");
        // Error messages are just logged
    }

    @Test
    void handleTextMessage_WithPairingRequest_WhenValidCode_ShouldCompletePayring() throws Exception {
        // Given
        WebSocketMessage pairingMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.PAIRING_REQUEST)
                .payload(Map.of("pairingCode", "123456", "playerName", "Test Player"))
                .build();
        
        TextMessage textMessage = new TextMessage("{\"type\":\"PAIRING_REQUEST\"}");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenReturn(pairingMessage);
        when(playerService.completePairing("123456", "Test Player", "session-123"))
                .thenReturn("new-player-id");
        when(objectMapper.writeValueAsString(any(WebSocketMessage.class)))
                .thenReturn("{\"success\":true}");

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(playerService).completePairing("123456", "Test Player", "session-123");
        verify(webSocketSession).sendMessage(any(TextMessage.class));
        
        // Verify player ID was updated in session attributes
        // Note: In real implementation, sessionAttributes would be updated
    }

    @Test
    void handleTextMessage_WithPairingRequest_WhenInvalidCode_ShouldSendError() throws Exception {
        // Given
        WebSocketMessage pairingMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.PAIRING_REQUEST)
                .payload(Map.of("pairingCode", "invalid", "playerName", "Test Player"))
                .build();
        
        TextMessage textMessage = new TextMessage("{\"type\":\"PAIRING_REQUEST\"}");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenReturn(pairingMessage);
        when(playerService.completePairing("invalid", "Test Player", "session-123"))
                .thenReturn(null);
        when(objectMapper.writeValueAsString(any(WebSocketMessage.class)))
                .thenReturn("{\"error\":\"Invalid pairing code\"}");

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(playerService).completePairing("invalid", "Test Player", "session-123");
        verify(webSocketSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleTextMessage_WithUnknownMessageType_ShouldLogWarning() throws Exception {
        // Given
        WebSocketMessage unknownMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.COMMAND) // Not handled in this method
                .payload(Map.of())
                .build();
        
        TextMessage textMessage = new TextMessage("{\"type\":\"COMMAND\"}");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenReturn(unknownMessage);

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(sessionManager).updateLastSeen("test-player");
        // Unknown message types are just logged as warnings
    }

    @Test
    void handleTextMessage_WithInvalidJson_ShouldSendErrorMessage() throws Exception {
        // Given
        TextMessage textMessage = new TextMessage("invalid json");
        
        when(objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class))
                .thenThrow(new RuntimeException("Invalid JSON"));
        when(objectMapper.writeValueAsString(any(WebSocketMessage.class)))
                .thenReturn("{\"error\":\"Invalid message format\"}");

        // When
        webSocketHandler.handleTextMessage(webSocketSession, textMessage);

        // Then
        verify(webSocketSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void afterConnectionClosed_ShouldCleanupSession() throws Exception {
        // Given
        CloseStatus closeStatus = CloseStatus.NORMAL;

        // When
        webSocketHandler.afterConnectionClosed(webSocketSession, closeStatus);

        // Then
        verify(sessionManager).removeSession("test-player", "session-123");
        verify(sessionManager).hasActiveSessions("test-player");
        verify(playerService).closePlayerSession("session-123");
    }

    @Test
    void afterConnectionClosed_WhenNoMoreActiveSessions_ShouldUpdatePlayerStatus() throws Exception {
        // Given
        CloseStatus closeStatus = CloseStatus.NORMAL;
        when(sessionManager.hasActiveSessions("test-player")).thenReturn(false);

        // When
        webSocketHandler.afterConnectionClosed(webSocketSession, closeStatus);

        // Then
        verify(sessionManager).removeSession("test-player", "session-123");
        verify(playerService).updatePlayerOnlineStatus("test-player", false);
        verify(playerService).closePlayerSession("session-123");
    }

    @Test
    void afterConnectionClosed_WhenExceptionOccurs_ShouldContinueExecution() throws Exception {
        // Given
        CloseStatus closeStatus = CloseStatus.NORMAL;
        doThrow(new RuntimeException("Test exception")).when(sessionManager).removeSession(anyString(), anyString());

        // When
        webSocketHandler.afterConnectionClosed(webSocketSession, closeStatus);

        // Then
        verify(sessionManager).removeSession("test-player", "session-123");
        // Should not throw exception
    }

    @Test
    void handleTransportError_ShouldCloseSessionWithError() throws Exception {
        // Given
        Throwable exception = new RuntimeException("Transport error");

        // When
        webSocketHandler.handleTransportError(webSocketSession, exception);

        // Then
        verify(webSocketSession).close(CloseStatus.SERVER_ERROR);
    }
}