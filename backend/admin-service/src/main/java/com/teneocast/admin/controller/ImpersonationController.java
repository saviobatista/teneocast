package com.teneocast.admin.controller;

import com.teneocast.admin.dto.ImpersonationRequest;
import com.teneocast.admin.entity.ImpersonationSession;
import com.teneocast.admin.service.ImpersonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/impersonate")
@RequiredArgsConstructor
@Slf4j
public class ImpersonationController {

    private final ImpersonationService impersonationService;

    @PostMapping
    public ResponseEntity<ImpersonationSession> startImpersonation(@RequestParam UUID adminUserId,
                                                                 @Valid @RequestBody ImpersonationRequest request) {
        log.info("Starting impersonation session for admin: {} targeting user: {} in tenant: {}", 
                adminUserId, request.getTargetUserId(), request.getTargetTenantId());
        
        try {
            ImpersonationSession session = impersonationService.startImpersonation(adminUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to start impersonation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Cannot start impersonation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> endImpersonation(@PathVariable UUID sessionId,
                                               @RequestParam UUID adminUserId,
                                               @RequestParam(required = false) String reason) {
        log.info("Ending impersonation session: {} by admin: {} with reason: {}", sessionId, adminUserId, reason);
        
        try {
            impersonationService.endImpersonation(sessionId, adminUserId, reason);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to end impersonation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/admin/{adminUserId}")
    public ResponseEntity<Void> endAllSessionsForAdmin(@PathVariable UUID adminUserId,
                                                     @RequestParam(required = false) String reason) {
        log.info("Ending all impersonation sessions for admin: {} with reason: {}", adminUserId, reason);
        
        try {
            impersonationService.endAllSessionsForAdmin(adminUserId, reason);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to end all sessions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<ImpersonationSession>> getAllActiveSessions() {
        log.info("Fetching all active impersonation sessions");
        List<ImpersonationSession> sessions = impersonationService.getAllActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/admin/{adminUserId}/active")
    public ResponseEntity<List<ImpersonationSession>> getActiveSessionsForAdmin(@PathVariable UUID adminUserId) {
        log.info("Fetching active impersonation sessions for admin: {}", adminUserId);
        List<ImpersonationSession> sessions = impersonationService.getActiveSessionsForAdmin(adminUserId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ImpersonationSession> getSessionById(@PathVariable UUID sessionId) {
        log.info("Fetching impersonation session: {}", sessionId);
        return impersonationService.getSessionById(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/target-user/{targetUserId}")
    public ResponseEntity<List<ImpersonationSession>> getSessionsByTargetUser(@PathVariable UUID targetUserId) {
        log.info("Fetching impersonation sessions for target user: {}", targetUserId);
        List<ImpersonationSession> sessions = impersonationService.getSessionsByTargetUser(targetUserId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/target-tenant/{targetTenantId}")
    public ResponseEntity<List<ImpersonationSession>> getSessionsByTargetTenant(@PathVariable UUID targetTenantId) {
        log.info("Fetching impersonation sessions for target tenant: {}", targetTenantId);
        List<ImpersonationSession> sessions = impersonationService.getSessionsByTargetTenant(targetTenantId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ImpersonationSession>> getRecentActiveSessions() {
        log.info("Fetching recent active impersonation sessions");
        List<ImpersonationSession> sessions = impersonationService.getRecentActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/since")
    public ResponseEntity<List<ImpersonationSession>> getSessionsStartedSince(@RequestParam String since) {
        log.info("Fetching impersonation sessions started since: {}", since);
        try {
            LocalDateTime sinceDateTime = LocalDateTime.parse(since);
            List<ImpersonationSession> sessions = impersonationService.getSessionsStartedSince(sinceDateTime);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.warn("Invalid date format: {}", since);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<Boolean> isSessionActive(@PathVariable UUID sessionId) {
        log.info("Checking status of impersonation session: {}", sessionId);
        boolean isActive = impersonationService.isSessionActive(sessionId);
        return ResponseEntity.ok(isActive);
    }

    @GetMapping("/admin/{adminUserId}/can-start")
    public ResponseEntity<Boolean> canAdminStartNewSession(@PathVariable UUID adminUserId) {
        log.info("Checking if admin can start new session: {}", adminUserId);
        boolean canStart = impersonationService.canAdminStartNewSession(adminUserId);
        return ResponseEntity.ok(canStart);
    }

    @GetMapping("/admin/{adminUserId}/session-count")
    public ResponseEntity<Long> getActiveSessionCountForAdmin(@PathVariable UUID adminUserId) {
        log.info("Getting active session count for admin: {}", adminUserId);
        long count = impersonationService.getActiveSessionCountForAdmin(adminUserId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/cleanup/expired")
    public ResponseEntity<Void> cleanupExpiredSessions() {
        log.info("Cleaning up expired impersonation sessions");
        impersonationService.cleanupExpiredSessions();
        return ResponseEntity.ok().build();
    }
}
