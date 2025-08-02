package com.teneocast.player.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSocketHandler wsHandler;

    @InjectMocks
    private WebSocketAuthInterceptor interceptor;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
    }

    @Test
    void beforeHandshake_WithValidTokenAndPlayerId_ShouldReturnTrue() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8082/ws/player?token=valid-jwt&playerId=player-123");
        when(request.getURI()).thenReturn(uri);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(request.getHeaders()).thenReturn(org.springframework.http.HttpHeaders.EMPTY);

        // When
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Then
        assertThat(result).isTrue();
        assertThat(attributes.get("token")).isEqualTo("valid-jwt");
        assertThat(attributes.get("playerId")).isEqualTo("player-123");
        assertThat(attributes.get("ipAddress")).isEqualTo("127.0.0.1");
    }

    @Test
    void beforeHandshake_WithMissingToken_ShouldReturnFalse() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8082/ws/player?playerId=player-123");
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void beforeHandshake_WithMissingPlayerId_ShouldReturnFalse() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8082/ws/player?token=valid-jwt");
        when(request.getURI()).thenReturn(uri);

        // When
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void beforeHandshake_WithXForwardedForHeader_ShouldUseForwardedIp() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8082/ws/player?token=valid-jwt&playerId=player-123");
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Forwarded-For", "192.168.1.1, 10.0.0.1");
        headers.add("User-Agent", "test-agent/1.0");
        
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(headers);

        // When
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Then
        assertThat(result).isTrue();
        assertThat(attributes.get("ipAddress")).isEqualTo("192.168.1.1");
        assertThat(attributes.get("userAgent")).isEqualTo("test-agent/1.0");
    }

    @Test
    void beforeHandshake_WithXRealIpHeader_ShouldUseRealIp() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8082/ws/player?token=valid-jwt&playerId=player-123");
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Real-IP", "192.168.1.100");
        
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(headers);

        // When
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Then
        assertThat(result).isTrue();
        assertThat(attributes.get("ipAddress")).isEqualTo("192.168.1.100");
    }

    @Test
    void beforeHandshake_WithNoProxyHeaders_ShouldUseRemoteAddress() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8082/ws/player?token=valid-jwt&playerId=player-123");
        when(request.getURI()).thenReturn(uri);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("10.0.0.5", 54321));
        when(request.getHeaders()).thenReturn(org.springframework.http.HttpHeaders.EMPTY);

        // When
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Then
        assertThat(result).isTrue();
        assertThat(attributes.get("ipAddress")).isEqualTo("10.0.0.5");
    }

    @Test
    void beforeHandshake_WithNullRemoteAddress_ShouldUseUnknown() throws Exception {
        // Given
        URI uri = URI.create("ws://localhost:8082/ws/player?token=valid-jwt&playerId=player-123");
        when(request.getURI()).thenReturn(uri);
        when(request.getRemoteAddress()).thenReturn(null);
        when(request.getHeaders()).thenReturn(org.springframework.http.HttpHeaders.EMPTY);

        // When
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Then
        assertThat(result).isTrue();
        assertThat(attributes.get("ipAddress")).isEqualTo("unknown");
    }

    @Test
    void afterHandshake_WithNoException_ShouldDoNothing() {
        // When
        interceptor.afterHandshake(request, response, wsHandler, null);

        // Then
        // No exception should be thrown, method should complete normally
    }

    @Test
    void afterHandshake_WithException_ShouldLogError() {
        // Given
        Exception exception = new RuntimeException("Test exception");

        // When
        interceptor.afterHandshake(request, response, wsHandler, exception);

        // Then
        // Exception should be logged, method should complete normally
    }
}