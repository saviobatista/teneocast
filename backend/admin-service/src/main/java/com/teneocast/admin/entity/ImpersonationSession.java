package com.teneocast.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "impersonation_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private AdminUser adminUser;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Column(name = "target_tenant_id", nullable = false)
    private UUID targetTenantId;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "end_reason")
    private String endReason;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void endSession(String reason) {
        this.isActive = false;
        this.endedAt = LocalDateTime.now();
        this.endReason = reason;
    }
}
