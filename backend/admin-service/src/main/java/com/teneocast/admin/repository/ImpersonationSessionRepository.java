package com.teneocast.admin.repository;

import com.teneocast.admin.entity.ImpersonationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImpersonationSessionRepository extends JpaRepository<ImpersonationSession, UUID> {

    List<ImpersonationSession> findByAdminUserId(UUID adminUserId);

    List<ImpersonationSession> findByTargetUserId(UUID targetUserId);

    List<ImpersonationSession> findByTargetTenantId(UUID targetTenantId);

    List<ImpersonationSession> findByIsActiveTrue();

    @Query("SELECT is FROM ImpersonationSession is WHERE is.isActive = true AND is.expiresAt < :now")
    List<ImpersonationSession> findExpiredSessions(@Param("now") LocalDateTime now);

    @Query("SELECT is FROM ImpersonationSession is WHERE is.adminUserId = :adminUserId AND is.isActive = true")
    List<ImpersonationSession> findActiveSessionsByAdmin(@Param("adminUserId") UUID adminUserId);

    @Query("SELECT COUNT(is) FROM ImpersonationSession is WHERE is.adminUserId = :adminUserId AND is.isActive = true")
    long countActiveSessionsByAdmin(@Param("adminUserId") UUID adminUserId);

    @Query("SELECT is FROM ImpersonationSession is WHERE is.isActive = true AND is.startedAt >= :since")
    List<ImpersonationSession> findSessionsStartedSince(@Param("since") LocalDateTime since);

    @Query("SELECT is FROM ImpersonationSession is WHERE is.isActive = true ORDER BY is.startedAt DESC")
    List<ImpersonationSession> findRecentActiveSessions();
}
