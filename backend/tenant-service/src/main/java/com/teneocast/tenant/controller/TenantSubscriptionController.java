package com.teneocast.tenant.controller;

import com.teneocast.tenant.dto.CreateSubscriptionRequest;
import com.teneocast.tenant.dto.TenantSubscriptionDto;
import com.teneocast.tenant.dto.UpdateSubscriptionRequest;
import com.teneocast.tenant.entity.TenantSubscription;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantSubscriptionService;
import com.teneocast.tenant.service.TenantValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class TenantSubscriptionController {

    private final TenantSubscriptionService tenantSubscriptionService;
    private final TenantValidationService tenantValidationService;

    /**
     * Create or update tenant subscription
     */
    @PostMapping
    public ResponseEntity<TenantSubscriptionDto> saveSubscription(@PathVariable String tenantId,
                                                           @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("Saving subscription for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantSubscription subscriptionEntity = convertToEntity(request);
            TenantSubscription subscription = tenantSubscriptionService.saveSubscription(tenantId, subscriptionEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(subscription));
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found for subscription: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error saving subscription: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get tenant subscription
     */
    @GetMapping
    public ResponseEntity<TenantSubscriptionDto> getSubscription(@PathVariable String tenantId) {
        log.debug("Getting subscription for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantSubscription subscription = tenantSubscriptionService.getSubscription(tenantId);
            return ResponseEntity.ok(convertToDto(subscription));
        } catch (TenantNotFoundException e) {
            log.warn("Subscription not found for tenant: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Update tenant subscription
     */
    @PutMapping
    public ResponseEntity<TenantSubscriptionDto> updateSubscription(@PathVariable String tenantId,
                                                             @Valid @RequestBody UpdateSubscriptionRequest request) {
        log.info("Updating subscription for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            TenantSubscription subscriptionEntity = convertToEntity(request);
            TenantSubscription subscription = tenantSubscriptionService.updateSubscription(tenantId, subscriptionEntity);
            return ResponseEntity.ok(convertToDto(subscription));
        } catch (TenantNotFoundException e) {
            log.warn("Subscription not found for update: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Validation error updating subscription: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete tenant subscription
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteSubscription(@PathVariable String tenantId) {
        log.info("Deleting subscription for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            tenantSubscriptionService.deleteSubscription(tenantId);
            return ResponseEntity.noContent().build();
        } catch (TenantNotFoundException e) {
            log.warn("Subscription not found for deletion: {}", tenantId);
            throw e;
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format for deletion: {}", tenantId);
            throw e;
        }
    }

    /**
     * Get subscriptions by plan type
     */
    @GetMapping("/plan-type/{planType}")
    public ResponseEntity<List<TenantSubscriptionDto>> getSubscriptionsByPlanType(@PathVariable TenantSubscription.PlanType planType) {
        log.debug("Getting subscriptions by plan type: {}", planType);
        
        List<TenantSubscription> subscriptions = tenantSubscriptionService.getSubscriptionsByPlanType(planType);
        List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(subscriptionDtos);
    }

    /**
     * Get subscriptions by plan type with pagination
     */
    @GetMapping("/plan-type/{planType}/page")
    public ResponseEntity<Page<TenantSubscriptionDto>> getSubscriptionsByPlanTypeWithPagination(
            @PathVariable TenantSubscription.PlanType planType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting subscriptions by plan type with pagination - planType: {}, page: {}, size: {}", planType, page, size);
        
        try {
            tenantValidationService.validatePagination(page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<TenantSubscription> subscriptions = tenantSubscriptionService.getSubscriptionsByPlanType(planType, pageable);
            Page<TenantSubscriptionDto> subscriptionDtos = subscriptions.map(this::convertToDto);
            return ResponseEntity.ok(subscriptionDtos);
        } catch (TenantValidationException e) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            throw e;
        }
    }

    /**
     * Get subscriptions by billing cycle
     */
    @GetMapping("/billing-cycle/{billingCycle}")
    public ResponseEntity<List<TenantSubscriptionDto>> getSubscriptionsByBillingCycle(@PathVariable TenantSubscription.BillingCycle billingCycle) {
        log.debug("Getting subscriptions by billing cycle: {}", billingCycle);
        
        List<TenantSubscription> subscriptions = tenantSubscriptionService.getSubscriptionsByBillingCycle(billingCycle);
        List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(subscriptionDtos);
    }

    /**
     * Get active subscriptions
     */
    @GetMapping("/active")
    public ResponseEntity<List<TenantSubscriptionDto>> getActiveSubscriptions() {
        log.debug("Getting active subscriptions");
        
        List<TenantSubscription> subscriptions = tenantSubscriptionService.getActiveSubscriptions();
        List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(subscriptionDtos);
    }

    /**
     * Get inactive subscriptions
     */
    @GetMapping("/inactive")
    public ResponseEntity<List<TenantSubscriptionDto>> getInactiveSubscriptions() {
        log.debug("Getting inactive subscriptions");
        
        List<TenantSubscription> subscriptions = tenantSubscriptionService.getInactiveSubscriptions();
        List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(subscriptionDtos);
    }

    /**
     * Get subscriptions by max users
     */
    @GetMapping("/max-users/{maxUsers}")
    public ResponseEntity<List<TenantSubscriptionDto>> getSubscriptionsByMaxUsers(@PathVariable Integer maxUsers) {
        log.debug("Getting subscriptions by max users: {}", maxUsers);
        
        List<TenantSubscription> subscriptions = tenantSubscriptionService.getSubscriptionsByMaxUsers(maxUsers);
        List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(subscriptionDtos);
    }

    /**
     * Get subscriptions by max storage
     */
    @GetMapping("/max-storage/{maxStorageGb}")
    public ResponseEntity<List<TenantSubscriptionDto>> getSubscriptionsByMaxStorage(@PathVariable Integer maxStorageGb) {
        log.debug("Getting subscriptions by max storage: {}", maxStorageGb);
        
        List<TenantSubscription> subscriptions = tenantSubscriptionService.getSubscriptionsByMaxStorage(maxStorageGb);
        List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(subscriptionDtos);
    }

    /**
     * Get subscriptions expiring by date
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<TenantSubscriptionDto>> getSubscriptionsExpiringBy(@RequestParam String expiryDate) {
        log.debug("Getting subscriptions expiring by: {}", expiryDate);
        
        try {
            LocalDateTime expiryDateTime = LocalDateTime.parse(expiryDate);
            List<TenantSubscription> subscriptions = tenantSubscriptionService.getSubscriptionsExpiringBy(expiryDateTime);
            List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                    .map(this::convertToDto)
                    .toList();
            return ResponseEntity.ok(subscriptionDtos);
        } catch (Exception e) {
            log.warn("Invalid expiry date format: {}", expiryDate);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get subscriptions by tenant ID expiring by date
     */
    @GetMapping("/expiring-by-tenant")
    public ResponseEntity<List<TenantSubscriptionDto>> getSubscriptionsByTenantIdExpiringBy(
            @PathVariable String tenantId,
            @RequestParam String expiryDate) {
        log.debug("Getting subscriptions by tenant ID: {} expiring by: {}", tenantId, expiryDate);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            LocalDateTime expiryDateTime = LocalDateTime.parse(expiryDate);
            List<TenantSubscription> subscriptions = tenantSubscriptionService.getSubscriptionsByTenantIdExpiringBy(tenantId, expiryDateTime);
            List<TenantSubscriptionDto> subscriptionDtos = subscriptions.stream()
                    .map(this::convertToDto)
                    .toList();
            return ResponseEntity.ok(subscriptionDtos);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        } catch (Exception e) {
            log.warn("Invalid expiry date format: {}", expiryDate);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get subscriptions count by plan type
     */
    @GetMapping("/count/plan-type/{planType}")
    public ResponseEntity<Long> getSubscriptionsCountByPlanType(@PathVariable TenantSubscription.PlanType planType) {
        log.debug("Getting subscriptions count by plan type: {}", planType);
        
        long count = tenantSubscriptionService.getSubscriptionsCountByPlanType(planType);
        return ResponseEntity.ok(count);
    }

    /**
     * Get subscriptions count by billing cycle
     */
    @GetMapping("/count/billing-cycle/{billingCycle}")
    public ResponseEntity<Long> getSubscriptionsCountByBillingCycle(@PathVariable TenantSubscription.BillingCycle billingCycle) {
        log.debug("Getting subscriptions count by billing cycle: {}", billingCycle);
        
        long count = tenantSubscriptionService.getSubscriptionsCountByBillingCycle(billingCycle);
        return ResponseEntity.ok(count);
    }

    /**
     * Get active subscriptions count
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveSubscriptionsCount() {
        log.debug("Getting active subscriptions count");
        
        long count = tenantSubscriptionService.getActiveSubscriptionsCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Get inactive subscriptions count
     */
    @GetMapping("/count/inactive")
    public ResponseEntity<Long> getInactiveSubscriptionsCount() {
        log.debug("Getting inactive subscriptions count");
        
        long count = tenantSubscriptionService.getInactiveSubscriptionsCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Check if subscription exists for tenant
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByTenantId(@PathVariable String tenantId) {
        log.debug("Checking if subscription exists for tenant: {}", tenantId);
        
        try {
            tenantValidationService.validateTenantId(tenantId);
            boolean exists = tenantSubscriptionService.existsByTenantId(tenantId);
            return ResponseEntity.ok(exists);
        } catch (TenantValidationException e) {
            log.warn("Invalid tenant ID format: {}", tenantId);
            throw e;
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health(@PathVariable String tenantId) {
        log.debug("Health check endpoint called for tenant subscription: {}", tenantId);
        return ResponseEntity.ok("Tenant subscription service is healthy for tenant: " + tenantId);
    }

    /**
     * Convert CreateSubscriptionRequest to TenantSubscription entity
     */
    private TenantSubscription convertToEntity(CreateSubscriptionRequest request) {
        return TenantSubscription.builder()
                .planType(request.getPlanType())
                .planName(request.getPlanName())
                .maxUsers(request.getMaxUsers())
                .maxStorageGb(request.getMaxStorageGb())
                .isActive(request.getIsActive())
                .billingCycle(request.getBillingCycle())
                .nextBillingDate(request.getNextBillingDate())
                .build();
    }

    /**
     * Convert UpdateSubscriptionRequest to TenantSubscription entity
     */
    private TenantSubscription convertToEntity(UpdateSubscriptionRequest request) {
        return TenantSubscription.builder()
                .planType(request.getPlanType())
                .planName(request.getPlanName())
                .maxUsers(request.getMaxUsers())
                .maxStorageGb(request.getMaxStorageGb())
                .isActive(request.getIsActive())
                .billingCycle(request.getBillingCycle())
                .nextBillingDate(request.getNextBillingDate())
                .build();
    }

    /**
     * Convert TenantSubscription entity to TenantSubscriptionDto
     */
    private TenantSubscriptionDto convertToDto(TenantSubscription subscription) {
        return TenantSubscriptionDto.builder()
                .id(subscription.getId())
                .tenantId(subscription.getTenant() != null ? subscription.getTenant().getId() : null)
                .planType(TenantSubscriptionDto.PlanType.valueOf(subscription.getPlanType().name()))
                .planName(subscription.getPlanName())
                .maxUsers(subscription.getMaxUsers())
                .maxStorageGb(subscription.getMaxStorageGb())
                .isActive(subscription.getIsActive())
                .billingCycle(TenantSubscriptionDto.BillingCycle.valueOf(subscription.getBillingCycle().name()))
                .nextBillingDate(subscription.getNextBillingDate())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
} 