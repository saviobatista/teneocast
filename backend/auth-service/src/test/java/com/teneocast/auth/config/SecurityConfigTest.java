package com.teneocast.auth.config;

import com.teneocast.auth.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Test
    void testSecurityFilterChainBean() throws Exception {
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(null);
        assertNotNull(filterChain);
    }

    @Test
    void testAuthenticationProviderBean() {
        AuthenticationProvider provider = securityConfig.authenticationProvider();
        assertNotNull(provider);
    }

    @Test
    void testAuthenticationManagerBean() throws Exception {
        AuthenticationManager manager = securityConfig.authenticationManager(null);
        assertNotNull(manager);
    }

    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        
        // Test password encoding
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testCorsConfigurationSourceBean() {
        CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
        assertNotNull(corsSource);
    }

    @Test
    void testUserDetailsServiceBean() {
        assertNotNull(userDetailsService);
    }
} 