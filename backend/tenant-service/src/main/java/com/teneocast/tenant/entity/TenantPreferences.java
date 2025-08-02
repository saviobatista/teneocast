package com.teneocast.tenant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TenantPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;
    
    @NotNull(message = "Tenant is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;
    
    @Builder.Default
    @Column(columnDefinition = "jsonb NOT NULL DEFAULT '{}'")
    private String playbackSettings = "{}";
    
    @Builder.Default
    @Column(columnDefinition = "jsonb NOT NULL DEFAULT '[]'")
    private String genrePreferences = "[]";
    
    @Builder.Default
    @Column(columnDefinition = "jsonb NOT NULL DEFAULT '{}'")
    private String adRules = "{}";
    
    @Builder.Default
    @Column(nullable = false)
    private Integer volumeDefault = 50;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    void prePersist() {
        if (playbackSettings == null) {
            playbackSettings = "{}";
        }
        if (genrePreferences == null) {
            genrePreferences = "[]";
        }
        if (adRules == null) {
            adRules = "{}";
        }
        if (volumeDefault == null) {
            volumeDefault = 50;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 