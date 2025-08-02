package com.teneocast.player.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_service_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlayerSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(nullable = false, unique = true)
    private String sessionId;
    
    @Column
    private String ipAddress;
    
    @Column
    private String userAgent;
    
    @Column
    private LocalDateTime connectedAt;
    
    @Column
    private LocalDateTime disconnectedAt;
    
    @Column
    private Boolean isActive;
    
    @Column
    private LocalDateTime lastPingAt;
    
    @Column
    private String connectionInfo;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
        if (connectedAt == null) {
            connectedAt = LocalDateTime.now();
        }
    }
}