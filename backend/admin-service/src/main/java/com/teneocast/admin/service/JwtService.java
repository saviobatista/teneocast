package com.teneocast.admin.service;

import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.common.dto.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final AdminUserRepository adminUserRepository;

    @Value("${admin.service.security.jwt.secret}")
    private String jwtSecret;

    @Value("${admin.service.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public Optional<AdminUser> validateTokenAndGetUser(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return Optional.empty();
            }

            String actualToken = token.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(actualToken)
                    .getBody();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            String tenantId = claims.get("tenantId", String.class);

            if (userId == null || role == null) {
                log.warn("JWT token missing required claims: userId={}, role={}", userId, role);
                return Optional.empty();
            }

            // Only allow ROOT and OPERATOR roles for admin service
            if (!UserRole.ROOT.name().equals(role) && !UserRole.OPERATOR.name().equals(role)) {
                log.warn("JWT token has insufficient role: {}", role);
                return Optional.empty();
            }

            UUID userUuid = UUID.fromString(userId);
            Optional<AdminUser> adminUser = adminUserRepository.findById(userUuid);

            if (adminUser.isEmpty()) {
                log.warn("JWT token references non-existent admin user: {}", userId);
                return Optional.empty();
            }

            // Check if user is active
            if (!adminUser.get().getIsActive()) {
                log.warn("JWT token references inactive admin user: {}", userId);
                return Optional.empty();
            }

            // Verify the role in the token matches the user's actual role
            AdminUser user = adminUser.get();
            if (!user.getRole().name().equals(role)) {
                log.warn("JWT token role mismatch: token={}, actual={}", role, user.getRole());
                return Optional.empty();
            }

            log.debug("JWT token validated successfully for user: {} with role: {}", user.getEmail(), role);
            return adminUser;

        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return true;
            }

            String actualToken = token.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(actualToken)
                    .getBody();

            long expiration = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();

            return currentTime > expiration;

        } catch (Exception e) {
            log.warn("Failed to check JWT token expiration: {}", e.getMessage());
            return true;
        }
    }

    public Optional<String> extractUserId(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return Optional.empty();
            }

            String actualToken = token.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(actualToken)
                    .getBody();

            return Optional.ofNullable(claims.getSubject());

        } catch (Exception e) {
            log.warn("Failed to extract user ID from JWT token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> extractRole(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return Optional.empty();
            }

            String actualToken = token.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(actualToken)
                    .getBody();

            return Optional.ofNullable(claims.get("role", String.class));

        } catch (Exception e) {
            log.warn("Failed to extract role from JWT token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
