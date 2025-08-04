package com.teneocast.tenant.dto;

import com.teneocast.tenant.entity.TenantSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubscriptionRequest {
    
    private TenantSubscription.PlanType planType;
    private String planName;
    private Integer maxUsers;
    private Integer maxStorageGb;
    private Boolean isActive;
    private TenantSubscription.BillingCycle billingCycle;
    private LocalDateTime nextBillingDate;
} 