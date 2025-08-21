package com.teneocast.admin.service;

import com.teneocast.admin.dto.ImpersonationRequest;
import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.entity.ImpersonationSession;
import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.admin.repository.ImpersonationSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImpersonationService {

    private final ImpersonationSessionRepository impersonationSessionRepository;
    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    @Value("${admin.service.impersonation.session-timeout-minutes:60}")
    private int sessionTimeoutMinutes;

    @Value("${admin.service.impersonation.max-concurrent-sessions:5}")
    private int maxConcurrentSessions;

    public ImpersonationSession startImpersonation(UUID adminUserId, ImpersonationRequest request) {
        // Validate admin user exists and is active
        AdminUser adminUser = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found with id: " + adminUserId));

        if (!adminUser.getIsActive()) {
            throw new IllegalArgumentException("Admin user is not active");
        }

        // Check concurrent session limit
        long activeSessions = impersonationSessionRepository.countActiveSessionsByAdmin(adminUserId);
        if (activeSessions >= maxConcurrentSessions) {
            throw new IllegalStateException("Maximum concurrent impersonation sessions reached for admin user");
        }

        // Create impersonation session
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(sessionTimeoutMinutes);
        
        String metadata = null;
        if (request.getMetadata() != null) {
            try {
                metadata = objectMapper.writeValueAsString(request.getMetadata());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize metadata for impersonation session: {}", e.getMessage());
            }
        }

        ImpersonationSession session = ImpersonationSession.builder()
                .adminUser(adminUser)
                .targetUserId(request.getTargetUserId())
                .targetTenantId(request.getTargetTenantId())
                .expiresAt(expiresAt)
                .isActive(true)
                .metadata(metadata)
                .build();

        ImpersonationSession savedSession = impersonationSessionRepository.save(session);
        log.info("Started impersonation session: {} for admin: {} targeting user: {} in tenant: {}", 
                savedSession.getId(), adminUser.getEmail(), request.getTargetUserId(), request.getTargetTenantId());

        return savedSession;
    }

    public void endImpersonation(UUID sessionId, UUID adminUserId, String reason) {
        ImpersonationSession session = impersonationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Impersonation session not found with id: " + sessionId));

        // Verify the admin user owns this session
        if (!session.getAdminUser().getId().equals(adminUserId)) {
            throw new IllegalArgumentException("Admin user is not authorized to end this session");
        }

        session.endSession(reason != null ? reason : "Manually ended");
        impersonationSessionRepository.save(session);
        
        log.info("Ended impersonation session: {} by admin: {} with reason: {}", 
                sessionId, session.getAdminUser().getEmail(), reason);
    }

    public void endAllSessionsForAdmin(UUID adminUserId, String reason) {
        List<ImpersonationSession> activeSessions = impersonationSessionRepository
                .findActiveSessionsByAdmin(adminUserId);

        for (ImpersonationSession session : activeSessions) {
            session.endSession(reason != null ? reason : "Bulk ended by admin");
        }

        if (!activeSessions.isEmpty()) {
            impersonationSessionRepository.saveAll(activeSessions);
            log.info("Ended {} impersonation sessions for admin: {} with reason: {}", 
                    activeSessions.size(), adminUserId, reason);
        }
    }

    public List<ImpersonationSession> getActiveSessionsForAdmin(UUID adminUserId) {
        return impersonationSessionRepository.findActiveSessionsByAdmin(adminUserId);
    }

    public List<ImpersonationSession> getAllActiveSessions() {
        return impersonationSessionRepository.findByIsActiveTrue();
    }

    public Optional<ImpersonationSession> getSessionById(UUID sessionId) {
        return impersonationSessionRepository.findById(sessionId);
    }

    public List<ImpersonationSession> getSessionsByTargetUser(UUID targetUserId) {
        return impersonationSessionRepository.findByTargetUserId(targetUserId);
    }

    public List<ImpersonationSession> getSessionsByTargetTenant(UUID targetTenantId) {
        return impersonationSessionRepository.findByTargetTenantId(targetTenantId);
    }

    public List<ImpersonationSession> getRecentActiveSessions() {
        return impersonationSessionRepository.findRecentActiveSessions();
    }

    public List<ImpersonationSession> getSessionsStartedSince(LocalDateTime since) {
        return impersonationSessionRepository.findSessionsStartedSince(since);
    }

    public void cleanupExpiredSessions() {
        List<ImpersonationSession> expiredSessions = impersonationSessionRepository
                .findExpiredSessions(LocalDateTime.now());

        for (ImpersonationSession session : expiredSessions) {
            session.endSession("Automatically expired");
        }

        if (!expiredSessions.isEmpty()) {
            impersonationSessionRepository.saveAll(expiredSessions);
            log.info("Cleaned up {} expired impersonation sessions", expiredSessions.size());
        }
    }

    public boolean isSessionActive(UUID sessionId) {
        Optional<ImpersonationSession> session = impersonationSessionRepository.findById(sessionId);
        return session.isPresent() && session.get().getIsActive() && !session.get().isExpired();
    }

    public boolean canAdminStartNewSession(UUID adminUserId) {
        long activeSessions = impersonationSessionRepository.countActiveSessionsByAdmin(adminUserId);
        return activeSessions < maxConcurrentSessions;
    }

    public long getActiveSessionCountForAdmin(UUID adminUserId) {
        return impersonationSessionRepository.countActiveSessionsByAdmin(adminUserId);
    }
}
