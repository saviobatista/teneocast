package com.teneocast.player.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.player.dto.PlayerCommand;
import com.teneocast.player.entity.Player;
import com.teneocast.player.entity.PlayerSession;
import com.teneocast.player.repository.PlayerRepository;
import com.teneocast.player.repository.PlayerSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerSessionRepository playerSessionRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private WebSocketSessionManager sessionManager;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        testPlayer = Player.builder()
                .id("test-player-id")
                .name("Test Player")
                .tenantId("test-tenant")
                .platform(Player.PlayerPlatform.WEB)
                .capabilities(Set.of(Player.PlayerCapability.AUDIO_PLAYBACK))
                .status(Player.PlayerStatus.OFFLINE)
                .isOnline(false)
                .volume(50)
                .build();
        
        // Set the private fields using reflection for testing
        try {
            java.lang.reflect.Field pairingCodeLengthField = PlayerService.class.getDeclaredField("pairingCodeLength");
            pairingCodeLengthField.setAccessible(true);
            pairingCodeLengthField.set(playerService, 6);
            
            java.lang.reflect.Field pairingCodeExpiryField = PlayerService.class.getDeclaredField("pairingCodeExpiry");
            pairingCodeExpiryField.setAccessible(true);
            pairingCodeExpiryField.set(playerService, 300);
        } catch (Exception e) {
            // Ignore reflection errors in test
        }
    }

    @Test
    void createPlayer_ShouldReturnCreatedPlayer() {
        // Given
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        Player result = playerService.createPlayer(
                "test-tenant",
                "Test Player",
                Player.PlayerPlatform.WEB,
                Set.of(Player.PlayerCapability.AUDIO_PLAYBACK)
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Player");
        assertThat(result.getTenantId()).isEqualTo("test-tenant");
        assertThat(result.getPlatform()).isEqualTo(Player.PlayerPlatform.WEB);
        
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void generatePairingCode_WhenPlayerExists_ShouldReturnPairingCode() {
        // Given
        when(playerRepository.findById("test-player-id")).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        String pairingCode = playerService.generatePairingCode("test-player-id");

        // Then
        assertThat(pairingCode).isNotNull();
        assertThat(pairingCode).isNotEmpty(); // Just check it's not empty
        assertThat(pairingCode).matches("\\d+"); // Should be digits only
        
        verify(playerRepository).findById("test-player-id");
        verify(playerRepository).save(any(Player.class));
        verify(valueOperations).set(anyString(), eq("test-player-id"));
    }

    @Test
    void generatePairingCode_WhenPlayerNotExists_ShouldThrowException() {
        // Given
        when(playerRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> playerService.generatePairingCode("non-existent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player not found: non-existent");
        
        verify(playerRepository).findById("non-existent");
        verify(playerRepository, never()).save(any());
    }

    @Test
    void completePairing_WithValidCode_ShouldReturnPlayerId() {
        // Given
        String pairingCode = "123456";
        testPlayer.setPairingCode(pairingCode);
        testPlayer.setPairingCodeExpiry(LocalDateTime.now().plusMinutes(5));
        
        when(valueOperations.get("pair:" + pairingCode)).thenReturn("test-player-id");
        when(playerRepository.findById("test-player-id")).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        String result = playerService.completePairing(pairingCode, "Updated Name", "session-id");

        // Then
        assertThat(result).isEqualTo("test-player-id");
        
        verify(playerRepository).save(any(Player.class));
        verify(redisTemplate).delete("pair:" + pairingCode);
    }

    @Test
    void completePairing_WithInvalidCode_ShouldReturnNull() {
        // Given
        when(valueOperations.get("pair:invalid")).thenReturn(null);
        when(playerRepository.findByPairingCode("invalid")).thenReturn(Optional.empty());

        // When
        String result = playerService.completePairing("invalid", "Name", "session-id");

        // Then
        assertThat(result).isNull();
        
        verify(playerRepository, never()).save(any());
    }

    @Test
    void updatePlayerOnlineStatus_WhenPlayerExists_ShouldUpdateStatus() {
        // Given
        when(playerRepository.findById("test-player-id")).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        playerService.updatePlayerOnlineStatus("test-player-id", true);

        // Then
        verify(playerRepository).findById("test-player-id");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void updatePlayerStatus_WhenPlayerExists_ShouldUpdateFields() {
        // Given
        Map<String, Object> statusData = Map.of(
                "nowPlaying", "test-track",
                "volume", 75,
                "status", "PLAYING"
        );
        
        when(playerRepository.findById("test-player-id")).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        playerService.updatePlayerStatus("test-player-id", statusData);

        // Then
        verify(playerRepository).findById("test-player-id");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void createPlayerSession_WhenPlayerExists_ShouldCreateSession() {
        // Given
        PlayerSession expectedSession = PlayerSession.builder()
                .id("session-id")
                .player(testPlayer)
                .sessionId("ws-session-id")
                .ipAddress("127.0.0.1")
                .userAgent("test-agent")
                .build();
        
        when(playerRepository.findById("test-player-id")).thenReturn(Optional.of(testPlayer));
        when(playerSessionRepository.save(any(PlayerSession.class))).thenReturn(expectedSession);

        // When
        PlayerSession result = playerService.createPlayerSession(
                "test-player-id", "ws-session-id", "127.0.0.1", "test-agent");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPlayer()).isEqualTo(testPlayer);
        assertThat(result.getSessionId()).isEqualTo("ws-session-id");
        
        verify(playerRepository).findById("test-player-id");
        verify(playerSessionRepository).save(any(PlayerSession.class));
    }

    @Test
    void createPlayerSession_WhenPlayerNotExists_ShouldThrowException() {
        // Given
        when(playerRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> playerService.createPlayerSession(
                "non-existent", "session-id", "127.0.0.1", "agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player not found: non-existent");
        
        verify(playerSessionRepository, never()).save(any());
    }

    @Test
    void closePlayerSession_WhenSessionExists_ShouldCloseSession() {
        // Given
        PlayerSession session = PlayerSession.builder()
                .sessionId("session-id")
                .player(testPlayer)
                .isActive(true)
                .build();
        
        when(playerSessionRepository.findBySessionId("session-id")).thenReturn(Optional.of(session));
        when(playerSessionRepository.save(any(PlayerSession.class))).thenReturn(session);

        // When
        playerService.closePlayerSession("session-id");

        // Then
        verify(playerSessionRepository).findBySessionId("session-id");
        verify(playerSessionRepository).save(any(PlayerSession.class));
    }

    @Test
    void sendCommandToPlayer_WithActiveSessions_ShouldSendCommand() throws Exception {
        // Given
        PlayerCommand command = PlayerCommand.builder()
                .messageId("msg-id")
                .commandType(PlayerCommand.CommandType.PAUSE)
                .payload(Map.of())
                .build();
        
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Set<WebSocketSession> sessions = Set.of(mockSession);
        
        when(sessionManager.getPlayerSessions("test-player-id")).thenReturn(sessions);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"message\"}");

        // When
        boolean result = playerService.sendCommandToPlayer("test-player-id", command);

        // Then
        assertThat(result).isTrue();
        
        verify(sessionManager).getPlayerSessions("test-player-id");
        verify(objectMapper).writeValueAsString(any());
        verify(mockSession).sendMessage(any());
    }

    @Test
    void sendCommandToPlayer_WithNoActiveSessions_ShouldReturnFalse() {
        // Given
        PlayerCommand command = PlayerCommand.builder()
                .commandType(PlayerCommand.CommandType.PAUSE)
                .build();
        
        when(sessionManager.getPlayerSessions("test-player-id")).thenReturn(Set.of());

        // When
        boolean result = playerService.sendCommandToPlayer("test-player-id", command);

        // Then
        assertThat(result).isFalse();
        
        verify(sessionManager).getPlayerSessions("test-player-id");
        verifyNoInteractions(objectMapper);
    }

    @Test
    void getPlayersByTenant_ShouldReturnPlayerList() {
        // Given
        when(playerRepository.findByTenantId("test-tenant")).thenReturn(List.of(testPlayer));

        // When
        List<Player> result = playerService.getPlayersByTenant("test-tenant");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testPlayer);
        
        verify(playerRepository).findByTenantId("test-tenant");
    }

    @Test
    void getPlayerById_WhenPlayerExists_ShouldReturnPlayer() {
        // Given
        when(playerRepository.findById("test-player-id")).thenReturn(Optional.of(testPlayer));

        // When
        Optional<Player> result = playerService.getPlayerById("test-player-id");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testPlayer);
        
        verify(playerRepository).findById("test-player-id");
    }

    @Test
    void getOnlinePlayersByTenant_ShouldReturnOnlinePlayerList() {
        // Given
        Player onlinePlayer = testPlayer.toBuilder().isOnline(true).build();
        when(playerRepository.findByTenantIdAndIsOnline("test-tenant", true))
                .thenReturn(List.of(onlinePlayer));

        // When
        List<Player> result = playerService.getOnlinePlayersByTenant("test-tenant");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsOnline()).isTrue();
        
        verify(playerRepository).findByTenantIdAndIsOnline("test-tenant", true);
    }

    @Test
    void cleanupExpiredPairingCodes_ShouldCleanupExpiredCodes() {
        // Given
        Player expiredPlayer = testPlayer.toBuilder()
                .pairingCode("expired")
                .pairingCodeExpiry(LocalDateTime.now().minusMinutes(1))
                .build();
        
        when(playerRepository.findExpiredPairingCodes(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredPlayer));
        when(playerRepository.saveAll(anyList())).thenReturn(List.of(expiredPlayer));

        // When
        playerService.cleanupExpiredPairingCodes();

        // Then
        verify(playerRepository).findExpiredPairingCodes(any(LocalDateTime.class));
        verify(redisTemplate).delete("pair:expired");
        verify(playerRepository).saveAll(anyList());
    }
}