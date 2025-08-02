package com.teneocast.player.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.player.dto.WebSocketMessage;
import com.teneocast.player.service.PlayerService;
import com.teneocast.player.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    private final PlayerService playerService;
    private final WebSocketSessionManager sessionManager;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String playerId = (String) session.getAttributes().get("playerId");
        String ipAddress = (String) session.getAttributes().get("ipAddress");
        String userAgent = (String) session.getAttributes().get("userAgent");
        
        log.info("WebSocket connection established for player: {}", playerId);
        
        try {
            // Register the session
            sessionManager.addSession(playerId, session);
            
            // Update player status
            playerService.updatePlayerOnlineStatus(playerId, true);
            
            // Create session record
            playerService.createPlayerSession(playerId, session.getId(), ipAddress, userAgent);
            
            // Send welcome message
            WebSocketMessage welcomeMessage = WebSocketMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .type(WebSocketMessage.MessageType.HEARTBEAT_PING)
                    .timestamp(LocalDateTime.now())
                    .playerId(playerId)
                    .build();
            
            sendMessage(session, welcomeMessage);
            
        } catch (Exception e) {
            log.error("Error establishing WebSocket connection for player: {}", playerId, e);
            session.close();
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String playerId = (String) session.getAttributes().get("playerId");
        
        try {
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            log.debug("Received message from player {}: {}", playerId, wsMessage.getType());
            
            // Update last seen
            sessionManager.updateLastSeen(playerId);
            
            switch (wsMessage.getType()) {
                case STATUS -> handleStatusMessage(playerId, wsMessage);
                case ACK -> handleAckMessage(playerId, wsMessage);
                case HEARTBEAT_PONG -> handleHeartbeatPong(playerId, wsMessage);
                case ERROR -> handleErrorMessage(playerId, wsMessage);
                case PAIRING_REQUEST -> handlePairingRequest(session, wsMessage);
                default -> log.warn("Unknown message type from player {}: {}", playerId, wsMessage.getType());
            }
            
        } catch (Exception e) {
            log.error("Error processing message from player: {}", playerId, e);
            sendErrorMessage(session, "Invalid message format");
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String playerId = (String) session.getAttributes().get("playerId");
        
        log.info("WebSocket connection closed for player: {} with status: {}", playerId, status);
        
        try {
            // Remove session
            sessionManager.removeSession(playerId, session.getId());
            
            // Update player status if no more active sessions
            if (!sessionManager.hasActiveSessions(playerId)) {
                playerService.updatePlayerOnlineStatus(playerId, false);
            }
            
            // Close session record
            playerService.closePlayerSession(session.getId());
            
        } catch (Exception e) {
            log.error("Error handling connection close for player: {}", playerId, e);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String playerId = (String) session.getAttributes().get("playerId");
        log.error("WebSocket transport error for player: {}", playerId, exception);
        
        session.close(CloseStatus.SERVER_ERROR);
    }
    
    private void handleStatusMessage(String playerId, WebSocketMessage message) {
        try {
            playerService.updatePlayerStatus(playerId, message.getPayload());
        } catch (Exception e) {
            log.error("Error handling status message for player: {}", playerId, e);
        }
    }
    
    private void handleAckMessage(String playerId, WebSocketMessage message) {
        log.debug("Received acknowledgment from player: {} for message: {}", 
                 playerId, message.getPayload().get("messageId"));
        // TODO: Handle message acknowledgments for delivery confirmation
    }
    
    private void handleHeartbeatPong(String playerId, WebSocketMessage message) {
        log.debug("Received heartbeat pong from player: {}", playerId);
        sessionManager.updateLastSeen(playerId);
    }
    
    private void handleErrorMessage(String playerId, WebSocketMessage message) {
        log.error("Received error from player: {} - {}", playerId, message.getPayload());
        // TODO: Handle error reporting from players
    }
    
    private void handlePairingRequest(WebSocketSession session, WebSocketMessage message) {
        try {
            String pairingCode = (String) message.getPayload().get("pairingCode");
            String playerName = (String) message.getPayload().get("playerName");
            
            String playerId = playerService.completePairing(pairingCode, playerName, session.getId());
            
            if (playerId != null) {
                // Update session attributes
                session.getAttributes().put("playerId", playerId);
                
                WebSocketMessage response = WebSocketMessage.builder()
                        .messageId(UUID.randomUUID().toString())
                        .type(WebSocketMessage.MessageType.PAIRING_RESPONSE)
                        .payload(Map.of("success", true, "playerId", playerId))
                        .timestamp(LocalDateTime.now())
                        .build();
                
                sendMessage(session, response);
            } else {
                sendErrorMessage(session, "Invalid pairing code");
            }
            
        } catch (Exception e) {
            log.error("Error handling pairing request", e);
            sendErrorMessage(session, "Pairing failed");
        }
    }
    
    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
        }
    }
    
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        WebSocketMessage errorMsg = WebSocketMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(WebSocketMessage.MessageType.ERROR)
                .payload(Map.of("error", errorMessage))
                .timestamp(LocalDateTime.now())
                .build();
        
        sendMessage(session, errorMsg);
    }
}