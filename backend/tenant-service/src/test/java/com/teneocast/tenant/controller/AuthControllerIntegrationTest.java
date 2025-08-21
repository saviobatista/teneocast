package com.teneocast.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.tenant.dto.LoginRequest;
import com.teneocast.tenant.dto.LoginResponse;
import com.teneocast.tenant.dto.TenantUserDto;
import com.teneocast.tenant.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Verify that the mock is working
        assertNotNull(authService, "AuthService mock should be injected");
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
    void testHealthEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Auth service is healthy"));
    }
}
