package com.teneocast.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set required properties for testing
        setField(jwtService, "secret", "test-jwt-secret-key-for-testing-only-very-long-key");
        setField(jwtService, "expiration", 86400000L);
        setField(jwtService, "refreshExpiration", 604800000L);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", 123L);

        String token = jwtService.generateToken("testuser", extraClaims);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken("testuser");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testExtractUsername() {
        String username = "testuser";
        String token = jwtService.generateToken(username);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractExpiration() {
        String token = jwtService.generateToken("testuser");
        assertNotNull(jwtService.extractExpiration(token));
    }

    @Test
    void testExtractClaim() {
        String token = jwtService.generateToken("testuser");
        String username = jwtService.extractClaim(token, claims -> claims.getSubject());
        assertEquals("testuser", username);
    }

    @Test
    void testIsTokenExpired() {
        String token = jwtService.generateToken("testuser");
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testValidateToken() {
        String username = "testuser";
        String token = jwtService.generateToken(username);
        assertTrue(jwtService.validateToken(token, username));
    }

    @Test
    void testValidateTokenWithWrongUsername() {
        String token = jwtService.generateToken("testuser");
        assertFalse(jwtService.validateToken(token, "wronguser"));
    }

    @Test
    void testIsTokenValid() {
        String token = jwtService.generateToken("testuser");
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testIsTokenValidWithInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }

    @Test
    void testIsTokenValidWithMalformedToken() {
        assertFalse(jwtService.isTokenValid("not.a.valid.token"));
    }

    @Test
    void testGetExpiration() {
        assertEquals(86400000L, jwtService.getExpiration());
    }

    @Test
    void testGetRefreshExpiration() {
        assertEquals(604800000L, jwtService.getRefreshExpiration());
    }
} 