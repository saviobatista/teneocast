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
import java.util.Set;

@Entity
@Table(name = "player_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(unique = true)
    private String pairingCode;
    
    @Column
    private LocalDateTime pairingCodeExpiry;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerPlatform platform;
    
    @Column
    private String deviceInfo;
    
    @Column
    private String appVersion;
    
    @Column
    private LocalDateTime lastSeen;
    
    @Column
    private String currentTrack;
    
    @Column
    private Integer volume;
    
    @Column
    private Boolean isOnline;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "player_service_capabilities", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "capability")
    private Set<PlayerCapability> capabilities;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlayerSession> sessions;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    void prePersist() {
        if (status == null) {
            status = PlayerStatus.OFFLINE;
        }
        if (isOnline == null) {
            isOnline = false;
        }
        if (volume == null) {
            volume = 50;
        }
    }
    
    public enum PlayerStatus {
        OFFLINE, ONLINE, PLAYING, PAUSED, BUFFERING, ERROR
    }
    
    public enum PlayerPlatform {
        WEB, WINDOWS, ANDROID, IOS
    }
    
    public enum PlayerCapability {
        AUDIO_PLAYBACK, TTS_PLAYBACK, REMOTE_CONTROL, OFFLINE_MODE, BACKGROUND_PLAYBACK
    }
}