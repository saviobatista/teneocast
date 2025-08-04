package com.teneocast.tenant.service;

import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantSubscription;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.repository.TenantRepository;
import com.teneocast.tenant.repository.TenantSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantSubscriptionService {

    private final TenantSubscriptionRepository tenantSubscriptionRepository;
    private final TenantRepository tenantRepository;
    private final TenantValidationService tenantValidationService;

    /**
     * Create or update tenant subscription
     */
    public TenantSubscription saveSubscription(String tenantId, TenantSubscription request) {
        log.info("Saving subscription for tenant: {}", tenantId);
        
        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + tenantId));
        
        // Validate request
        validateSubscriptionRequest(request);
        
        // Check if subscription already exists
        Optional<TenantSubscription> existingSubscription = tenantSubscriptionRepository.findByTenantId(tenantId);
        
        TenantSubscription subscription;
        if (existingSubscription.isPresent()) {
            // Update existing subscription
            subscription = existingSubscription.get();
            updateSubscription(subscription, request);
        } else {
            // Create new subscription
            subscription = createSubscription(tenant, request);
        }
        
        subscription.setUpdatedAt(LocalDateTime.now());
        TenantSubscription savedSubscription = tenantSubscriptionRepository.save(subscription);
        
        log.info("Saved subscription for tenant: {}", tenantId);
        return savedSubscription;
    }

    /**
     * Get tenant subscription
     */
    @Transactional(readOnly = true)
    public TenantSubscription getSubscription(String tenantId) {
        log.debug("Getting subscription for tenant: {}", tenantId);
        
        TenantSubscription subscription = tenantSubscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Subscription not found for tenant: " + tenantId));
        
        return subscription;
    }

    /**
     * Update subscription
     */
    public TenantSubscription updateSubscription(String tenantId, TenantSubscription request) {
        log.info("Updating subscription for tenant: {}", tenantId);
        
        TenantSubscription subscription = tenantSubscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Subscription not found for tenant: " + tenantId));
        
        // Validate request
        validateSubscriptionRequest(request);
        
        // Update fields
        updateSubscription(subscription, request);
        subscription.setUpdatedAt(LocalDateTime.now());
        
        TenantSubscription savedSubscription = tenantSubscriptionRepository.save(subscription);
        log.info("Updated subscription for tenant: {}", tenantId);
        
        return savedSubscription;
    }

    /**
     * Delete tenant subscription
     */
    public void deleteSubscription(String tenantId) {
        log.info("Deleting subscription for tenant: {}", tenantId);
        
        TenantSubscription subscription = tenantSubscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Subscription not found for tenant: " + tenantId));
        
        tenantSubscriptionRepository.delete(subscription);
        log.info("Deleted subscription for tenant: {}", tenantId);
    }

    /**
     * Get subscriptions by plan type
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getSubscriptionsByPlanType(TenantSubscription.PlanType planType) {
        log.debug("Getting subscriptions by plan type: {}", planType);
        return tenantSubscriptionRepository.findByPlanType(planType);
    }

    /**
     * Get subscriptions by plan type with pagination
     */
    @Transactional(readOnly = true)
    public Page<TenantSubscription> getSubscriptionsByPlanType(TenantSubscription.PlanType planType, Pageable pageable) {
        log.debug("Getting subscriptions by plan type: {} with pagination: {}", planType, pageable);
        return tenantSubscriptionRepository.findByPlanType(planType, pageable);
    }

    /**
     * Get subscriptions by billing cycle
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getSubscriptionsByBillingCycle(TenantSubscription.BillingCycle billingCycle) {
        log.debug("Getting subscriptions by billing cycle: {}", billingCycle);
        return tenantSubscriptionRepository.findByBillingCycle(billingCycle);
    }

    /**
     * Get active subscriptions
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getActiveSubscriptions() {
        log.debug("Getting active subscriptions");
        return tenantSubscriptionRepository.findByIsActiveTrue();
    }

    /**
     * Get inactive subscriptions
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getInactiveSubscriptions() {
        log.debug("Getting inactive subscriptions");
        return tenantSubscriptionRepository.findByIsActiveFalse();
    }

    /**
     * Get subscriptions by max users
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getSubscriptionsByMaxUsers(Integer maxUsers) {
        log.debug("Getting subscriptions by max users: {}", maxUsers);
        return tenantSubscriptionRepository.findByMaxUsers(maxUsers);
    }

    /**
     * Get subscriptions by max storage
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getSubscriptionsByMaxStorage(Integer maxStorageGb) {
        log.debug("Getting subscriptions by max storage: {}", maxStorageGb);
        return tenantSubscriptionRepository.findByMaxStorageGb(maxStorageGb);
    }

    /**
     * Get subscriptions expiring soon
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getSubscriptionsExpiringBy(LocalDateTime expiryDate) {
        log.debug("Getting subscriptions expiring by: {}", expiryDate);
        return tenantSubscriptionRepository.findSubscriptionsExpiringBy(expiryDate);
    }

    /**
     * Get subscriptions by tenant ID expiring soon
     */
    @Transactional(readOnly = true)
    public List<TenantSubscription> getSubscriptionsByTenantIdExpiringBy(String tenantId, LocalDateTime expiryDate) {
        log.debug("Getting subscriptions by tenant ID: {} expiring by: {}", tenantId, expiryDate);
        return tenantSubscriptionRepository.findSubscriptionsByTenantIdExpiringBy(tenantId, expiryDate);
    }

    /**
     * Get subscriptions count by plan type
     */
    @Transactional(readOnly = true)
    public long getSubscriptionsCountByPlanType(TenantSubscription.PlanType planType) {
        log.debug("Getting subscriptions count by plan type: {}", planType);
        return tenantSubscriptionRepository.countByPlanType(planType);
    }

    /**
     * Get subscriptions count by billing cycle
     */
    @Transactional(readOnly = true)
    public long getSubscriptionsCountByBillingCycle(TenantSubscription.BillingCycle billingCycle) {
        log.debug("Getting subscriptions count by billing cycle: {}", billingCycle);
        return tenantSubscriptionRepository.countByBillingCycle(billingCycle);
    }

    /**
     * Get active subscriptions count
     */
    @Transactional(readOnly = true)
    public long getActiveSubscriptionsCount() {
        log.debug("Getting active subscriptions count");
        return tenantSubscriptionRepository.countByIsActiveTrue();
    }

    /**
     * Get inactive subscriptions count
     */
    @Transactional(readOnly = true)
    public long getInactiveSubscriptionsCount() {
        log.debug("Getting inactive subscriptions count");
        return tenantSubscriptionRepository.countByIsActiveFalse();
    }

    /**
     * Check if subscription exists for tenant
     */
    @Transactional(readOnly = true)
    public boolean existsByTenantId(String tenantId) {
        return tenantSubscriptionRepository.existsByTenantId(tenantId);
    }

    /**
     * Create new subscription
     */
    private TenantSubscription createSubscription(Tenant tenant, TenantSubscription request) {
        return TenantSubscription.builder()
                .tenant(tenant)
                .planType(request.getPlanType() != null ? request.getPlanType() : TenantSubscription.PlanType.BASIC)
                .planName(request.getPlanName() != null ? request.getPlanName() : "Basic Plan")
                .maxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : 10)
                .maxStorageGb(request.getMaxStorageGb() != null ? request.getMaxStorageGb() : 5)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .billingCycle(request.getBillingCycle() != null ? request.getBillingCycle() : TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(request.getNextBillingDate())
                .build();
    }

    /**
     * Update existing subscription
     */
    private void updateSubscription(TenantSubscription subscription, TenantSubscription request) {
        if (request.getPlanType() != null) {
            subscription.setPlanType(request.getPlanType());
        }
        if (request.getPlanName() != null) {
            subscription.setPlanName(request.getPlanName());
        }
        if (request.getMaxUsers() != null) {
            subscription.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxStorageGb() != null) {
            subscription.setMaxStorageGb(request.getMaxStorageGb());
        }
        if (request.getIsActive() != null) {
            subscription.setIsActive(request.getIsActive());
        }
        if (request.getBillingCycle() != null) {
            subscription.setBillingCycle(request.getBillingCycle());
        }
        if (request.getNextBillingDate() != null) {
            subscription.setNextBillingDate(request.getNextBillingDate());
        }
    }

    /**
     * Validate subscription request
     */
    private void validateSubscriptionRequest(TenantSubscription request) {
        if (request == null) {
            throw new TenantValidationException("Subscription request cannot be null");
        }
        
        if (request.getPlanName() != null && request.getPlanName().trim().isEmpty()) {
            throw new TenantValidationException("Plan name cannot be empty");
        }
        
        if (request.getMaxUsers() != null && request.getMaxUsers() < 1) {
            throw new TenantValidationException("Max users must be at least 1");
        }
        
        if (request.getMaxStorageGb() != null && request.getMaxStorageGb() < 1) {
            throw new TenantValidationException("Max storage must be at least 1 GB");
        }
        
        if (request.getNextBillingDate() != null && request.getNextBillingDate().isBefore(LocalDateTime.now())) {
            throw new TenantValidationException("Next billing date cannot be in the past");
        }
    }
} 