package com.teneocast.auth.service;

import com.teneocast.auth.dto.LoginRequest;
import com.teneocast.auth.dto.LoginResponse;
import com.teneocast.auth.entity.RefreshToken;
import com.teneocast.auth.entity.User;
import com.teneocast.auth.repository.RefreshTokenRepository;
import com.teneocast.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));

        UserDetails result = authService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsernameOrEmail("testuser");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsernameOrEmail("nonexistent"))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> authService.loadUserByUsername("nonexistent"));
    }

    @Test
    void testLogin_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        when(jwtService.generateToken("testuser"))
                .thenReturn("access_token");
        when(jwtService.generateRefreshToken("testuser"))
                .thenReturn("refresh_token");
        when(jwtService.getExpiration())
                .thenReturn(86400000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(new RefreshToken());

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testLogin_AuthenticationFailure() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void testRefreshToken_Success() {
        when(jwtService.isTokenValid("valid_refresh_token"))
                .thenReturn(true);
        when(jwtService.extractUsername("valid_refresh_token"))
                .thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        
        RefreshToken storedToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token("valid_refresh_token")
                .isRevoked(false)
                .build();
        when(refreshTokenRepository.findByToken("valid_refresh_token"))
                .thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(storedToken);
        when(jwtService.generateToken("testuser"))
                .thenReturn("new_access_token");
        when(jwtService.generateRefreshToken("testuser"))
                .thenReturn("new_refresh_token");
        when(jwtService.getExpiration())
                .thenReturn(86400000L);

        LoginResponse response = authService.refreshToken("valid_refresh_token");

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("new_refresh_token", response.getRefreshToken());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void testRefreshToken_InvalidToken() {
        when(jwtService.isTokenValid("invalid_token"))
                .thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.refreshToken("invalid_token"));
    }

    @Test
    void testRefreshToken_UserNotFound() {
        when(jwtService.isTokenValid("valid_token"))
                .thenReturn(true);
        when(jwtService.extractUsername("valid_token"))
                .thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> authService.refreshToken("valid_token"));
    }

    @Test
    void testRefreshToken_TokenNotFound() {
        when(jwtService.isTokenValid("valid_token"))
                .thenReturn(true);
        when(jwtService.extractUsername("valid_token"))
                .thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByToken("valid_token"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.refreshToken("valid_token"));
    }

    @Test
    void testRefreshToken_TokenRevoked() {
        when(jwtService.isTokenValid("valid_token"))
                .thenReturn(true);
        when(jwtService.extractUsername("valid_token"))
                .thenReturn("testuser");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        
        RefreshToken revokedToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token("valid_token")
                .isRevoked(true)
                .build();
        when(refreshTokenRepository.findByToken("valid_token"))
                .thenReturn(Optional.of(revokedToken));

        assertThrows(RuntimeException.class,
                () -> authService.refreshToken("valid_token"));
    }

    @Test
    void testLogout_Success() {
        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token("refresh_token")
                .isRevoked(false)
                .build();
        when(refreshTokenRepository.findByToken("refresh_token"))
                .thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(token);

        assertDoesNotThrow(() -> authService.logout("refresh_token"));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testLogout_TokenNotFound() {
        when(refreshTokenRepository.findByToken("nonexistent_token"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.logout("nonexistent_token"));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
} 