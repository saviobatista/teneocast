package com.teneocast.auth.repository;

import com.teneocast.auth.entity.RefreshToken;
import com.teneocast.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private User testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create a test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("password123")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        
        userRepository.save(testUser);
        
        // Create a test token
        testToken = RefreshToken.builder()
                .user(testUser)
                .token("test_token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .isRevoked(false)
                .build();
    }

    @Test
    void testSaveAndFindById() {
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        assertNotNull(savedToken.getId());
        
        Optional<RefreshToken> foundToken = refreshTokenRepository.findById(savedToken.getId());
        assertTrue(foundToken.isPresent());
        assertEquals(savedToken.getId(), foundToken.get().getId());
    }

    @Test
    void testFindByToken() {
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("test_token");
        assertTrue(foundToken.isPresent());
        assertEquals(savedToken.getId(), foundToken.get().getId());
    }

    @Test
    void testFindByToken_NotFound() {
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("nonexistent_token");
        assertFalse(foundToken.isPresent());
    }

    @Test
    void testFindByUserIdAndNotRevoked() {
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserIdAndNotRevoked(testUser.getId());
        assertTrue(foundToken.isPresent());
        assertEquals(savedToken.getId(), foundToken.get().getId());
    }

    @Test
    void testFindByUserIdAndNotRevoked_RevokedToken() {
        testToken.setIsRevoked(true);
        refreshTokenRepository.save(testToken);
        
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserIdAndNotRevoked(testUser.getId());
        assertFalse(foundToken.isPresent());
    }

    @Test
    void testFindByUserIdAndNotRevoked_UserNotFound() {
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserIdAndNotRevoked(999L);
        assertFalse(foundToken.isPresent());
    }

    @Test
    void testRevokeAllTokensByUserId() {
        // Create multiple tokens for the same user
        RefreshToken token1 = RefreshToken.builder()
                .user(testUser)
                .token("token1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .isRevoked(false)
                .build();
        
        RefreshToken token2 = RefreshToken.builder()
                .user(testUser)
                .token("token2")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .isRevoked(false)
                .build();
        
        refreshTokenRepository.save(token1);
        refreshTokenRepository.save(token2);
        
        // Revoke all tokens for the user
        refreshTokenRepository.revokeAllTokensByUserId(testUser.getId());
        
        // Clear the persistence context to ensure fresh data is loaded
        entityManager.clear();
        
        // Verify tokens are revoked
        Optional<RefreshToken> foundToken1 = refreshTokenRepository.findByToken("token1");
        Optional<RefreshToken> foundToken2 = refreshTokenRepository.findByToken("token2");
        
        assertTrue(foundToken1.isPresent());
        assertTrue(foundToken2.isPresent());
        assertTrue(foundToken1.get().getIsRevoked());
        assertTrue(foundToken2.get().getIsRevoked());
    }

    @Test
    void testDeleteExpiredTokens() {
        // Create an expired token
        RefreshToken expiredToken = RefreshToken.builder()
                .user(testUser)
                .token("expired_token")
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired yesterday
                .createdAt(LocalDateTime.now().minusDays(7))
                .isRevoked(false)
                .build();
        
        refreshTokenRepository.save(expiredToken);
        assertEquals(1, refreshTokenRepository.count());
        
        // Delete expired tokens
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        
        // Verify expired token is deleted
        assertEquals(0, refreshTokenRepository.count());
    }

    @Test
    void testCountActiveTokensByUserId() {
        // Create multiple tokens for the same user
        RefreshToken token1 = RefreshToken.builder()
                .user(testUser)
                .token("token1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .isRevoked(false)
                .build();
        
        RefreshToken token2 = RefreshToken.builder()
                .user(testUser)
                .token("token2")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .isRevoked(false)
                .build();
        
        RefreshToken revokedToken = RefreshToken.builder()
                .user(testUser)
                .token("revoked_token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .isRevoked(true)
                .build();
        
        refreshTokenRepository.save(token1);
        refreshTokenRepository.save(token2);
        refreshTokenRepository.save(revokedToken);
        
        long activeTokenCount = refreshTokenRepository.countActiveTokensByUserId(testUser.getId());
        assertEquals(2, activeTokenCount);
    }

    @Test
    void testSaveAndUpdateToken() {
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        assertFalse(savedToken.getIsRevoked());
        
        // Update the token
        savedToken.setIsRevoked(true);
        RefreshToken updatedToken = refreshTokenRepository.save(savedToken);
        assertTrue(updatedToken.getIsRevoked());
    }

    @Test
    void testDeleteToken() {
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        assertEquals(1, refreshTokenRepository.count());
        
        refreshTokenRepository.delete(savedToken);
        assertEquals(0, refreshTokenRepository.count());
    }
} 