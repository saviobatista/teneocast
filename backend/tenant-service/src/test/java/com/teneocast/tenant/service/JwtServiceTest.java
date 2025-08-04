package com.teneocast.tenant.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set required properties
        ReflectionTestUtils.setField(jwtService, "secret", "teneocast-jwt-secret-key-2024-very-long-and-secure-key-for-production");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days
    }

    @Test
    void testGenerateToken() {
        // Given
        String username = "tenant123:user@example.com";

        // When
        String token = jwtService.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        // Given
        String username = "tenant123:user@example.com";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("tenantId", "tenant123");

        // When
        String token = jwtService.generateToken(username, extraClaims);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(username, jwtService.extractUsername(token));
        
        // Verify extra claims by parsing the token
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(ReflectionTestUtils.getField(jwtService, "secret").toString().getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("tenant123", claims.get("tenantId"));
    }

    @Test
    void testGenerateRefreshToken() {
        // Given
        String username = "tenant123:user@example.com";

        // When
        String refreshToken = jwtService.generateRefreshToken(username);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertEquals(username, jwtService.extractUsername(refreshToken));
        
        // Verify refresh token has longer expiration
        Date expiration = jwtService.extractExpiration(refreshToken);
        Date now = new Date();
        long diffInMillis = expiration.getTime() - now.getTime();
        assertTrue(diffInMillis > 86400000L); // Should be longer than regular token
    }

    @Test
    void testExtractUsername() {
        // Given
        String username = "tenant123:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractExpiration() {
        // Given
        String username = "tenant123:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testExtractClaim() {
        // Given
        String username = "tenant123:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        String extractedUsername = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Then
        assertEquals(username, extractedUsername);
        assertNotNull(issuedAt);
    }

    @Test
    void testIsTokenExpired() {
        // Given
        String username = "tenant123:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testValidateToken() {
        // Given
        String username = "tenant123:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isValid = jwtService.validateToken(token, username);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWithWrongUsername() {
        // Given
        String username = "tenant123:user@example.com";
        String wrongUsername = "tenant456:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isValid = jwtService.validateToken(token, wrongUsername);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid() {
        // Given
        String username = "tenant123:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValidWithInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValidWithEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtService.isTokenValid(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValidWithNullToken() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtService.isTokenValid(nullToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetExpiration() {
        // When
        Long expiration = jwtService.getExpiration();

        // Then
        assertEquals(86400000L, expiration);
    }

    @Test
    void testGetRefreshExpiration() {
        // When
        Long refreshExpiration = jwtService.getRefreshExpiration();

        // Then
        assertEquals(604800000L, refreshExpiration);
    }

    @Test
    void testTokenStructure() {
        // Given
        String username = "tenant123:user@example.com";
        String token = jwtService.generateToken(username);

        // When
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(ReflectionTestUtils.getField(jwtService, "secret").toString().getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Then
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void testTokenWithSpecialCharacters() {
        // Given
        String username = "tenant123:user+test@example.com";
        String token = jwtService.generateToken(username);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testTokenWithUnicodeCharacters() {
        // Given
        String username = "tenant123:usér@example.com";
        String token = jwtService.generateToken(username);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testMultipleTokensForSameUser() {
        // Given
        String username = "tenant123:user@example.com";

        // When
        String token1 = jwtService.generateToken(username);
        String token2 = jwtService.generateToken(username);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        // Tokens might be the same if generated in the same millisecond
        // The important thing is that both are valid
        assertEquals(username, jwtService.extractUsername(token1));
        assertEquals(username, jwtService.extractUsername(token2));
    }

    @Test
    void testTokenExpirationTime() {
        // Given
        String username = "tenant123:user@example.com";
        long startTime = System.currentTimeMillis();

        // When
        String token = jwtService.generateToken(username);
        Date expiration = jwtService.extractExpiration(token);

        // Then
        long expectedExpiration = startTime + 86400000L; // 24 hours
        long actualExpiration = expiration.getTime();
        long tolerance = 1000L; // 1 second tolerance for test execution time
        
        assertTrue(Math.abs(expectedExpiration - actualExpiration) < tolerance);
    }

    @Test
    void testRefreshTokenExpirationTime() {
        // Given
        String username = "tenant123:user@example.com";
        long startTime = System.currentTimeMillis();

        // When
        String refreshToken = jwtService.generateRefreshToken(username);
        Date expiration = jwtService.extractExpiration(refreshToken);

        // Then
        long expectedExpiration = startTime + 604800000L; // 7 days
        long actualExpiration = expiration.getTime();
        long tolerance = 1000L; // 1 second tolerance for test execution time
        
        assertTrue(Math.abs(expectedExpiration - actualExpiration) < tolerance);
    }
} 