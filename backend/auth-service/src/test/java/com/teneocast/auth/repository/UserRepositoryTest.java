package com.teneocast.auth.repository;

import com.teneocast.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.flush();
        
        testUser = User.builder()
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
    }

    @Test
    void testSaveAndFindById() {
        User savedUser = userRepository.save(testUser);
        assertNotNull(savedUser.getId());
        
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsername() {
        userRepository.save(testUser);
        
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsername_NotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByEmail() {
        userRepository.save(testUser);
        
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByUsernameOrEmail_WithUsername() {
        userRepository.save(testUser);
        
        Optional<User> foundUser = userRepository.findByUsernameOrEmail("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsernameOrEmail_WithEmail() {
        userRepository.save(testUser);
        
        Optional<User> foundUser = userRepository.findByUsernameOrEmail("test@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByUsernameOrEmail_NotFound() {
        Optional<User> foundUser = userRepository.findByUsernameOrEmail("nonexistent");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testExistsByUsername() {
        userRepository.save(testUser);
        
        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        userRepository.save(testUser);
        
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testExistsByUsernameOrEmail_WithUsername() {
        userRepository.save(testUser);
        
        assertTrue(userRepository.existsByUsernameOrEmail("testuser"));
        assertFalse(userRepository.existsByUsernameOrEmail("nonexistent"));
    }

    @Test
    void testExistsByUsernameOrEmail_WithEmail() {
        userRepository.save(testUser);
        
        assertTrue(userRepository.existsByUsernameOrEmail("test@example.com"));
        assertFalse(userRepository.existsByUsernameOrEmail("nonexistent@example.com"));
    }

    @Test
    void testSaveMultipleUsers() {
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .passwordHash("encoded_password")
                .firstName("User")
                .lastName("One")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .passwordHash("encoded_password")
                .firstName("User")
                .lastName("Two")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        assertEquals(2, userRepository.count());
    }

    @Test
    void testDeleteUser() {
        User savedUser = userRepository.save(testUser);
        assertEquals(1, userRepository.count());
        
        userRepository.delete(savedUser);
        assertEquals(0, userRepository.count());
    }
} 