package com.teneocast.player.repository;

import com.teneocast.player.entity.PlayerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerSessionRepository extends JpaRepository<PlayerSession, String> {
    
    Optional<PlayerSession> findBySessionId(String sessionId);
    
    List<PlayerSession> findByPlayerIdAndIsActive(String playerId, Boolean isActive);
    
    @Query("SELECT ps FROM PlayerSession ps WHERE ps.player.id = :playerId ORDER BY ps.createdAt DESC")
    List<PlayerSession> findByPlayerIdOrderByCreatedAtDesc(@Param("playerId") String playerId);
    
    @Query("SELECT ps FROM PlayerSession ps WHERE ps.isActive = true AND ps.lastPingAt < :threshold")
    List<PlayerSession> findStaleActiveSessions(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT ps FROM PlayerSession ps WHERE ps.player.tenantId = :tenantId AND ps.isActive = true")
    List<PlayerSession> findActiveSessionsByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(ps) FROM PlayerSession ps WHERE ps.player.tenantId = :tenantId AND ps.isActive = true")
    Long countActiveSessionsByTenant(@Param("tenantId") String tenantId);
    
    void deleteByPlayerIdAndIsActive(String playerId, Boolean isActive);
}