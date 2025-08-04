package com.teneocast.tenant.config;

import com.teneocast.tenant.service.TenantUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthFilter;

    @Mock
    private TenantUserDetailsService userDetailsService;

    @Test
    void testSecurityConfigBeanCreation() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);

        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        AuthenticationProvider authenticationProvider = securityConfig.authenticationProvider();
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Then
        assertNotNull(passwordEncoder);
        assertNotNull(authenticationProvider);
        assertNotNull(corsConfigurationSource);
    }

    @Test
    void testPasswordEncoder() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);

        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertNotNull(passwordEncoder);
        
        // Test password encoding and matching
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
    }

    @Test
    void testAuthenticationProvider() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);

        // When
        AuthenticationProvider authenticationProvider = securityConfig.authenticationProvider();

        // Then
        assertNotNull(authenticationProvider);
        // The authentication provider should be properly configured
        assertTrue(authenticationProvider.supports(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testCorsConfigurationSource() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);

        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Then
        assertNotNull(corsConfigurationSource);
        
        // Test CORS configuration
        org.springframework.web.cors.CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(null);
        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("*"));
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedMethods().contains("PUT"));
        assertTrue(config.getAllowedMethods().contains("DELETE"));
        assertTrue(config.getAllowedHeaders().contains("*"));
    }

    @Test
    void testSecurityConfigWithContext() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(SecurityConfig.class);
        
        // Register mock beans
        context.registerBean("jwtAuthenticationFilter", JwtAuthenticationFilter.class, () -> jwtAuthFilter);
        context.registerBean("tenantUserDetailsService", TenantUserDetailsService.class, () -> userDetailsService);
        
        context.refresh();

        // When
        SecurityConfig securityConfig = context.getBean(SecurityConfig.class);
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        AuthenticationProvider authenticationProvider = context.getBean(AuthenticationProvider.class);
        CorsConfigurationSource corsConfigurationSource = context.getBean(CorsConfigurationSource.class);

        // Then
        assertNotNull(securityConfig);
        assertNotNull(passwordEncoder);
        assertNotNull(authenticationProvider);
        assertNotNull(corsConfigurationSource);

        context.close();
    }

    @Test
    void testPasswordEncoderMultipleEncodings() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // When
        String password = "testPassword123";
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        // Then
        // Each encoding should produce a different result (due to salt)
        assertNotEquals(encoded1, encoded2);
        
        // But both should match the original password
        assertTrue(passwordEncoder.matches(password, encoded1));
        assertTrue(passwordEncoder.matches(password, encoded2));
    }

    @Test
    void testPasswordEncoderWithSpecialCharacters() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // When
        String password = "Test@Password#123$";
        String encoded = passwordEncoder.encode(password);

        // Then
        assertNotNull(encoded);
        assertNotEquals(password, encoded);
        assertTrue(passwordEncoder.matches(password, encoded));
    }

    @Test
    void testPasswordEncoderWithUnicodeCharacters() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // When
        String password = "TestPassword123\u00E9\u00F1";
        String encoded = passwordEncoder.encode(password);

        // Then
        assertNotNull(encoded);
        assertNotEquals(password, encoded);
        assertTrue(passwordEncoder.matches(password, encoded));
    }

    @Test
    void testCorsConfigurationDetails() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);

        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(null);

        // Then
        assertNotNull(config);
        
        // Check allowed origins
        assertTrue(config.getAllowedOrigins().contains("*"));
        
        // Check allowed methods
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedMethods().contains("PUT"));
        assertTrue(config.getAllowedMethods().contains("DELETE"));
        assertTrue(config.getAllowedMethods().contains("OPTIONS"));
        
        // Check allowed headers
        assertTrue(config.getAllowedHeaders().contains("*"));
        
        // Check exposed headers
        assertTrue(config.getExposedHeaders().contains("Authorization"));
        assertTrue(config.getExposedHeaders().contains("Content-Type"));
        
        // Check credentials
        assertTrue(config.getAllowCredentials());
        
        // Check max age
        assertEquals(3600L, config.getMaxAge());
    }

    @Test
    void testAuthenticationProviderConfiguration() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);

        // When
        AuthenticationProvider authenticationProvider = securityConfig.authenticationProvider();

        // Then
        assertNotNull(authenticationProvider);
        
        // Test that it supports UsernamePasswordAuthenticationToken
        assertTrue(authenticationProvider.supports(
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class));
        
        // Test that it doesn't support other token types
        assertFalse(authenticationProvider.supports(
            org.springframework.security.authentication.AnonymousAuthenticationToken.class));
    }

    @Test
    void testSecurityConfigConstructor() {
        // Given & When
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);

        // Then
        assertNotNull(securityConfig);
        // The constructor should not throw any exceptions
    }

    @Test
    void testPasswordEncoderNullInput() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // When & Then
        // Should handle null input gracefully
        assertThrows(IllegalArgumentException.class, () -> passwordEncoder.encode(null));
        assertThrows(IllegalArgumentException.class, () -> passwordEncoder.matches(null, "encoded"));
        assertThrows(IllegalArgumentException.class, () -> passwordEncoder.matches("raw", null));
    }

    @Test
    void testPasswordEncoderEmptyInput() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // When
        String emptyPassword = "";
        String encoded = passwordEncoder.encode(emptyPassword);

        // Then
        assertNotNull(encoded);
        assertNotEquals(emptyPassword, encoded);
        assertTrue(passwordEncoder.matches(emptyPassword, encoded));
    }
} 