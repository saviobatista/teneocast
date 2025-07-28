package com.teneocast.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.auth.dto.LoginRequest;
import com.teneocast.auth.entity.User;
import com.teneocast.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        userRepository.deleteAll();
        
        // Create a test user
        User testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        
        userRepository.save(testUser);
        
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLoginEndpoint_Success() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(loginRequest);
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.user").exists());
    }

    @Test
    void testLoginEndpoint_WithEmail() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("test@example.com")
                .password("password123")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(loginRequest);
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginEndpoint_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("wrongpassword")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(loginRequest);
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginEndpoint_UserNotFound() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("nonexistent")
                .password("password123")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(loginRequest);
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginEndpoint_InvalidRequest() throws Exception {
        String invalidJson = "{\"invalid\": \"json\"}";
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRefreshTokenEndpoint() throws Exception {
        // Test refresh token endpoint with a dummy token
        // In a real scenario, you'd get a valid token from login
        mockMvc.perform(post("/auth/refresh")
                .param("refreshToken", "dummy_refresh_token"))
                .andExpect(status().isInternalServerError()); // Should fail with invalid token
    }

    @Test
    void testLogoutEndpoint() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .param("refreshToken", "dummy_refresh_token"))
                .andExpect(status().isOk());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Auth service is running"));
    }

    @Test
    void testLoginEndpoint_MissingUsername() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .password("password123")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(loginRequest);
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpoint_MissingPassword() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(loginRequest);
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }
} 