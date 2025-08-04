package com.teneocast.tenant.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TenantSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;
    
    @NotNull(message = "Tenant is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tenant tenant;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType = PlanType.BASIC;
    
    @NotBlank(message = "Plan name is required")
    @Column(nullable = false)
    private String planName;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer maxUsers = 10;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer maxStorageGb = 5;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle = BillingCycle.MONTHLY;
    
    @Column
    private LocalDateTime nextBillingDate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    void prePersist() {
        if (planType == null) {
            planType = PlanType.BASIC;
        }
        if (maxUsers == null) {
            maxUsers = 10;
        }
        if (maxStorageGb == null) {
            maxStorageGb = 5;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (billingCycle == null) {
            billingCycle = BillingCycle.MONTHLY;
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
    
    public enum PlanType {
        BASIC, PREMIUM, ENTERPRISE
    }
    
    public enum BillingCycle {
        MONTHLY, QUARTERLY, YEARLY
    }
} 