package com.teneocast.player.repository;

import com.teneocast.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {
    
    Optional<Player> findByPairingCode(String pairingCode);
    
    List<Player> findByTenantId(String tenantId);
    
    List<Player> findByTenantIdAndIsOnline(String tenantId, Boolean isOnline);
    
    @Query("SELECT p FROM Player p WHERE p.tenantId = :tenantId AND p.status = :status")
    List<Player> findByTenantIdAndStatus(@Param("tenantId") String tenantId, 
                                        @Param("status") Player.PlayerStatus status);
    
    @Query("SELECT p FROM Player p WHERE p.pairingCodeExpiry < :expiry AND p.pairingCode IS NOT NULL")
    List<Player> findExpiredPairingCodes(@Param("expiry") LocalDateTime expiry);
    
    @Query("SELECT p FROM Player p WHERE p.lastSeen < :threshold")
    List<Player> findInactivePlayers(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT COUNT(p) FROM Player p WHERE p.tenantId = :tenantId AND p.isOnline = true")
    Long countOnlinePlayersByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT p FROM Player p WHERE p.tenantId = :tenantId AND p.platform = :platform")
    List<Player> findByTenantIdAndPlatform(@Param("tenantId") String tenantId, 
                                          @Param("platform") Player.PlayerPlatform platform);
}