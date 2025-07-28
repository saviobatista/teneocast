package com.teneocast.auth.service;

import com.teneocast.auth.entity.User;
import com.teneocast.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

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
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isCredentialsNonExpired());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        
        verify(userRepository).findByUsernameOrEmail("testuser");
    }

    @Test
    void testLoadUserByUsername_WithEmail() {
        when(userRepository.findByUsernameOrEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsernameOrEmail("test@example.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsernameOrEmail("nonexistent"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent"));
        
        verify(userRepository).findByUsernameOrEmail("nonexistent");
    }

    @Test
    void testLoadUserByUsername_InactiveUser() {
        User inactiveUser = User.builder()
                .id(2L)
                .username("inactiveuser")
                .email("inactive@example.com")
                .passwordHash("encoded_password")
                .firstName("Inactive")
                .lastName("User")
                .role(User.UserRole.USER)
                .isActive(false)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userRepository.findByUsernameOrEmail("inactiveuser"))
                .thenReturn(Optional.of(inactiveUser));

        UserDetails result = userDetailsService.loadUserByUsername("inactiveuser");

        assertNotNull(result);
        assertFalse(result.isEnabled());
        assertFalse(result.isAccountNonLocked());
        verify(userRepository).findByUsernameOrEmail("inactiveuser");
    }

    @Test
    void testLoadUserByUsername_AdminUser() {
        User adminUser = User.builder()
                .id(3L)
                .username("adminuser")
                .email("admin@example.com")
                .passwordHash("encoded_password")
                .firstName("Admin")
                .lastName("User")
                .role(User.UserRole.ADMIN)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userRepository.findByUsernameOrEmail("adminuser"))
                .thenReturn(Optional.of(adminUser));

        UserDetails result = userDetailsService.loadUserByUsername("adminuser");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findByUsernameOrEmail("adminuser");
    }

    @Test
    void testLoadUserByUsername_ModeratorUser() {
        User moderatorUser = User.builder()
                .id(4L)
                .username("moderatoruser")
                .email("moderator@example.com")
                .passwordHash("encoded_password")
                .firstName("Moderator")
                .lastName("User")
                .role(User.UserRole.MODERATOR)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userRepository.findByUsernameOrEmail("moderatoruser"))
                .thenReturn(Optional.of(moderatorUser));

        UserDetails result = userDetailsService.loadUserByUsername("moderatoruser");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MODERATOR")));
        verify(userRepository).findByUsernameOrEmail("moderatoruser");
    }
} 