package com.teneocast.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantSubscriptionDto {
    
    private String id;
    private String tenantId;
    private PlanType planType;
    private String planName;
    private Integer maxUsers;
    private Integer maxStorageGb;
    private Boolean isActive;
    private BillingCycle billingCycle;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextBillingDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public enum PlanType {
        BASIC, PREMIUM, ENTERPRISE
    }
    
    public enum BillingCycle {
        MONTHLY, QUARTERLY, YEARLY
    }
} 