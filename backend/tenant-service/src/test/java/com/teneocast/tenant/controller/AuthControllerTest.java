package com.teneocast.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.tenant.dto.LoginRequest;
import com.teneocast.tenant.dto.LoginResponse;
import com.teneocast.tenant.dto.TenantUserDto;
import com.teneocast.tenant.service.AuthService;
import com.teneocast.tenant.service.JwtService;
import com.teneocast.tenant.service.TenantUserDetailsService;
import com.teneocast.tenant.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {AuthController.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TenantUserDetailsService tenantUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Verify that the mock is working
        assertNotNull(authService, "AuthService mock should be injected");
    }

    @Test
    void testMockInjection() {
        // This test verifies that the mock is properly injected
        assertNotNull(authService);
        
        // Test that the mock can be configured
        when(authService.login(any(LoginRequest.class))).thenReturn(
            LoginResponse.builder()
                .accessToken("test.token")
                .refreshToken("test.refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(TenantUserDto.builder()
                    .id("test123")
                    .email("test@example.com")
                    .role(TenantUserDto.UserRole.MASTER)
                    .isActive(true)
                    .build())
                .build()
        );
        
        LoginRequest request = LoginRequest.builder()
            .tenantId("test")
            .email("test@example.com")
            .password("password")
            .build();
            
        LoginResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("test.token", response.getAccessToken());
    }

    @Test
    void testControllerDirectly() {
        // Test the controller method directly without MockMvc
        AuthController controller = new AuthController(authService);
        
        // Setup mock
        LoginRequest request = LoginRequest.builder()
            .tenantId("tenant123")
            .email("test@example.com")
            .password("password123")
            .build();
            
        LoginResponse expectedResponse = LoginResponse.builder()
            .accessToken("test.token")
            .refreshToken("test.refresh")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .user(TenantUserDto.builder()
                .id("test123")
                .email("test@example.com")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build())
            .build();
            
        when(authService.login(request)).thenReturn(expectedResponse);
        
        // Call controller method directly
        var response = controller.login(request);
        
        // Verify response
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        
        // Verify mock was called
        verify(authService).login(request);
    }

    @Test
    void testLoginEndpoint_Success() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("password123")
                .build();

        TenantUserDto userDto = TenantUserDto.builder()
                .id("user123")
                .email("user@example.com")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userDto)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token.here"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token.here"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L))
                .andExpect(jsonPath("$.user.id").value("user123"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    void testRefreshTokenEndpoint() throws Exception {
        // Given
        TenantUserDto userDto = TenantUserDto.builder()
                .id("user123")
                .email("user@example.com")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("new.access.token")
                .refreshToken("new.refresh.token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userDto)
                .build();

        when(authService.refreshToken("old.refresh.token")).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", "old.refresh.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"));
    }

    @Test
    void testLogoutEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .param("refreshToken", "refresh.token.here"))
                .andExpect(status().isOk());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Auth service is healthy"));
    }

    @Test
    void testLoginEndpoint_MissingTenantId() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpoint_MissingEmail() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpoint_MissingPassword() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpoint_EmptyTenantId() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("")
                .email("user@example.com")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpoint_EmptyEmail() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpoint_EmptyPassword() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("user@example.com")
                .password("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpoint_InvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRefreshTokenEndpoint_EmptyToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", ""))
                .andExpect(status().isOk());
    }

    @Test
    void testLogoutEndpoint_EmptyToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .param("refreshToken", ""))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginEndpoint_WithSpecialCharacters() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant-123")
                .email("user+test@example.com")
                .password("password@123#")
                .build();

        TenantUserDto userDto = TenantUserDto.builder()
                .id("user123")
                .email("user+test@example.com")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userDto)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token.here"))
                .andExpect(jsonPath("$.user.email").value("user+test@example.com"));
    }

    @Test
    void testLoginEndpoint_WithUnicodeCharacters() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .tenantId("tenant123")
                .email("usér@example.com")
                .password("password123")
                .build();

        TenantUserDto userDto = TenantUserDto.builder()
                .id("user123")
                .email("usér@example.com")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userDto)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("usér@example.com"));
    }
} 