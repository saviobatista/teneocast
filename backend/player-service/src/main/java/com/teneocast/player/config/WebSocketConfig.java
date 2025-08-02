package com.teneocast.player.config;

import com.teneocast.player.websocket.PlayerWebSocketHandler;
import com.teneocast.player.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final PlayerWebSocketHandler playerWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    
    @Value("${websocket.allowed-origins}")
    private String[] allowedOrigins;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(playerWebSocketHandler, "/ws/player")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }
}