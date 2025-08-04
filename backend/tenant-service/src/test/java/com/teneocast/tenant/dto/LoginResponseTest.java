package com.teneocast.tenant.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLoginResponseBuilder() {
        // Given & When
        LoginResponse response = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(TenantUserDto.builder()
                        .id("user123")
                        .email("user@example.com")
                        .build())
                .build();

        // Then
        assertEquals("access.token.here", response.getAccessToken());
        assertEquals("refresh.token.here", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("user123", response.getUser().getId());
    }

    @Test
    void testLoginResponseDefaultValues() {
        // Given & When
        LoginResponse response = new LoginResponse();

        // Then
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType()); // Default value
        assertNull(response.getExpiresIn());
        assertNull(response.getUser());
    }

    @Test
    void testLoginResponseAllArgsConstructor() {
        // Given
        TenantUserDto user = TenantUserDto.builder()
                .id("user123")
                .email("user@example.com")
                .build();

        // When
        LoginResponse response = new LoginResponse(
                "access.token.here",
                "refresh.token.here",
                "Bearer",
                86400000L,
                user
        );

        // Then
        assertEquals("access.token.here", response.getAccessToken());
        assertEquals("refresh.token.here", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertEquals(user, response.getUser());
    }

    @Test
    void testLoginResponseDefaultTokenType() {
        // Given & When
        LoginResponse response = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .expiresIn(86400000L)
                .build();

        // Then
        assertEquals("Bearer", response.getTokenType()); // Default value from @Builder.Default
    }

    @Test
    void testLoginResponseEqualsAndHashCode() {
        // Given
        TenantUserDto user = TenantUserDto.builder()
                .id("user123")
                .email("user@example.com")
                .build();

        LoginResponse response1 = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();

        LoginResponse response2 = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();

        LoginResponse response3 = LoginResponse.builder()
                .accessToken("different.token")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();

        // Then
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testLoginResponseToString() {
        // Given
        LoginResponse response = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .build();

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("access.token.here"));
        assertTrue(toString.contains("refresh.token.here"));
        assertTrue(toString.contains("Bearer"));
        assertTrue(toString.contains("86400000"));
    }

    @Test
    void testLoginResponseJsonSerialization() throws Exception {
        // Given
        TenantUserDto user = TenantUserDto.builder()
                .id("user123")
                .email("user@example.com")
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();

        // When
        String json = objectMapper.writeValueAsString(response);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        // Then
        assertEquals(response.getAccessToken(), deserialized.getAccessToken());
        assertEquals(response.getRefreshToken(), deserialized.getRefreshToken());
        assertEquals(response.getTokenType(), deserialized.getTokenType());
        assertEquals(response.getExpiresIn(), deserialized.getExpiresIn());
        assertNotNull(deserialized.getUser());
        assertEquals(response.getUser().getId(), deserialized.getUser().getId());
    }

    @Test
    void testLoginResponseJsonDeserialization() throws Exception {
        // Given
        String json = """
                {
                    "accessToken": "access.token.here",
                    "refreshToken": "refresh.token.here",
                    "tokenType": "Bearer",
                    "expiresIn": 86400000,
                    "user": {
                        "id": "user123",
                        "email": "user@example.com"
                    }
                }
                """;

        // When
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Then
        assertEquals("access.token.here", response.getAccessToken());
        assertEquals("refresh.token.here", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("user123", response.getUser().getId());
        assertEquals("user@example.com", response.getUser().getEmail());
    }

    @Test
    void testLoginResponseJsonDeserializationWithoutUser() throws Exception {
        // Given
        String json = """
                {
                    "accessToken": "access.token.here",
                    "refreshToken": "refresh.token.here",
                    "tokenType": "Bearer",
                    "expiresIn": 86400000
                }
                """;

        // When
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Then
        assertEquals("access.token.here", response.getAccessToken());
        assertEquals("refresh.token.here", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNull(response.getUser());
    }

    @Test
    void testLoginResponseSetters() {
        // Given
        LoginResponse response = new LoginResponse();
        TenantUserDto user = TenantUserDto.builder()
                .id("user123")
                .email("user@example.com")
                .build();

        // When
        response.setAccessToken("access.token.here");
        response.setRefreshToken("refresh.token.here");
        response.setTokenType("Bearer");
        response.setExpiresIn(86400000L);
        response.setUser(user);

        // Then
        assertEquals("access.token.here", response.getAccessToken());
        assertEquals("refresh.token.here", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertEquals(user, response.getUser());
    }

    @Test
    void testLoginResponseWithNullValues() {
        // Given & When
        LoginResponse response = LoginResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .tokenType(null)
                .expiresIn(null)
                .user(null)
                .build();

        // Then
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertNull(response.getTokenType());
        assertNull(response.getExpiresIn());
        assertNull(response.getUser());
    }
} 