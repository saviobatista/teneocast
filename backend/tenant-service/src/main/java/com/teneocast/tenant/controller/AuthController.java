package com.teneocast.tenant.controller;

import com.teneocast.tenant.dto.LoginRequest;
import com.teneocast.tenant.dto.LoginResponse;
import com.teneocast.tenant.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for tenant: {} with email: {}", request.getTenantId(), request.getEmail());
        
        try {
            LoginResponse response = authService.login(request);
            log.info("Login successful for tenant: {} with email: {}, response: {}", request.getTenantId(), request.getEmail(), response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for tenant: {} with email: {}", request.getTenantId(), request.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        log.debug("Token refresh attempt");
        
        LoginResponse response = authService.refreshToken(refreshToken);
        log.debug("Token refresh successful");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String refreshToken) {
        log.debug("Logout attempt");
        
        authService.logout(refreshToken);
        log.debug("Logout successful");
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("Auth health check");
        return ResponseEntity.ok("Auth service is healthy");
    }
} 