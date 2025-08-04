package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.LoginRequest;
import com.teneocast.tenant.dto.LoginResponse;
import com.teneocast.tenant.dto.TenantUserDto;
import com.teneocast.tenant.entity.TenantUser;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.repository.TenantUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final TenantUserRepository tenantUserRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TenantUserService tenantUserService;

    public LoginResponse login(LoginRequest request) {
        log.debug("Processing login for tenant: {} with email: {}", request.getTenantId(), request.getEmail());
        
        // Validate tenant exists
        if (!tenantUserService.existsByEmail(request.getTenantId(), request.getEmail())) {
            log.warn("Login failed - user not found: {} in tenant: {}", request.getEmail(), request.getTenantId());
            throw new UsernameNotFoundException("Invalid credentials");
        }
        
        // Create username in format tenantId:email for authentication
        String username = request.getTenantId() + ":" + request.getEmail();
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails.getUsername());
        String refreshToken = jwtService.generateRefreshToken(userDetails.getUsername());
        
        // Get user details
        TenantUser user = tenantUserRepository.findByTenantIdAndEmail(request.getTenantId(), request.getEmail())
                .orElseThrow(() -> new TenantNotFoundException("User not found"));
        
        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        tenantUserRepository.save(user);
        
        log.info("Login successful for user: {} in tenant: {}", request.getEmail(), request.getTenantId());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(tenantUserService.mapToDto(user))
                .build();
    }

    public LoginResponse refreshToken(String refreshToken) {
        log.debug("Processing token refresh");
        
        if (!jwtService.isTokenValid(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new RuntimeException("Invalid refresh token");
        }
        
        String username = jwtService.extractUsername(refreshToken);
        
        // Validate user still exists
        String[] parts = username.split(":", 2);
        if (parts.length != 2) {
            log.warn("Invalid username format in refresh token: {}", username);
            throw new RuntimeException("Invalid token format");
        }
        
        String tenantId = parts[0];
        String email = parts[1];
        
        Optional<TenantUser> userOpt = tenantUserRepository.findByTenantIdAndEmail(tenantId, email);
        if (userOpt.isEmpty() || !userOpt.get().getIsActive()) {
            log.warn("User not found or inactive during token refresh: {} in tenant: {}", email, tenantId);
            throw new RuntimeException("User not found or inactive");
        }
        
        // Generate new tokens
        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        
        log.debug("Token refresh successful for user: {} in tenant: {}", email, tenantId);
        
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(tenantUserService.mapToDto(userOpt.get()))
                .build();
    }

    public void logout(String refreshToken) {
        log.debug("Processing logout");
        
        if (jwtService.isTokenValid(refreshToken)) {
            String username = jwtService.extractUsername(refreshToken);
            log.info("User logged out: {}", username);
        }
        
        // In a more sophisticated implementation, you might want to blacklist the refresh token
        // For now, we just log the logout
    }
} 