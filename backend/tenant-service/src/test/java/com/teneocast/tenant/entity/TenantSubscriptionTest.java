package com.teneocast.tenant.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TenantSubscriptionTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testTenantSubscriptionBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        
        // When
        TenantSubscription subscription = TenantSubscription.builder()
                .id("sub-id")
                .tenant(tenant)
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(25)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(now.plusMonths(1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(subscription);
        assertEquals("sub-id", subscription.getId());
        assertEquals(tenant, subscription.getTenant());
        assertEquals(TenantSubscription.PlanType.PREMIUM, subscription.getPlanType());
        assertEquals("Premium Plan", subscription.getPlanName());
        assertEquals(25, subscription.getMaxUsers());
        assertEquals(10, subscription.getMaxStorageGb());
        assertTrue(subscription.getIsActive());
        assertEquals(TenantSubscription.BillingCycle.MONTHLY, subscription.getBillingCycle());
        assertEquals(now.plusMonths(1), subscription.getNextBillingDate());
        assertEquals(now, subscription.getCreatedAt());
        assertEquals(now, subscription.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionDefaultValues() {
        // When
        TenantSubscription subscription = new TenantSubscription();

        // Then
        assertNull(subscription.getId());
        assertNull(subscription.getTenant());
        assertEquals(TenantSubscription.PlanType.BASIC, subscription.getPlanType());
        assertNull(subscription.getPlanName());
        assertEquals(10, subscription.getMaxUsers());
        assertEquals(5, subscription.getMaxStorageGb());
        assertTrue(subscription.getIsActive());
        assertEquals(TenantSubscription.BillingCycle.MONTHLY, subscription.getBillingCycle());
        assertNull(subscription.getNextBillingDate());
        assertNull(subscription.getCreatedAt());
        assertNull(subscription.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionPrePersist() {
        // Given
        TenantSubscription subscription = new TenantSubscription();
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        subscription.setTenant(tenant);
        subscription.setPlanName("Basic Plan");

        // When
        subscription.prePersist();

        // Then
        assertEquals(TenantSubscription.PlanType.BASIC, subscription.getPlanType());
        assertEquals(10, subscription.getMaxUsers());
        assertEquals(5, subscription.getMaxStorageGb());
        assertTrue(subscription.getIsActive());
        assertEquals(TenantSubscription.BillingCycle.MONTHLY, subscription.getBillingCycle());
        assertNotNull(subscription.getCreatedAt());
        assertNotNull(subscription.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionPrePersistWithNullValues() {
        // Given
        TenantSubscription subscription = new TenantSubscription();
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        subscription.setTenant(tenant);
        subscription.setPlanName("Basic Plan");
        subscription.setPlanType(null);
        subscription.setMaxUsers(null);
        subscription.setMaxStorageGb(null);
        subscription.setIsActive(null);
        subscription.setBillingCycle(null);

        // When
        subscription.prePersist();

        // Then
        assertEquals(TenantSubscription.PlanType.BASIC, subscription.getPlanType());
        assertEquals(10, subscription.getMaxUsers());
        assertEquals(5, subscription.getMaxStorageGb());
        assertTrue(subscription.getIsActive());
        assertEquals(TenantSubscription.BillingCycle.MONTHLY, subscription.getBillingCycle());
        assertNotNull(subscription.getCreatedAt());
        assertNotNull(subscription.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionPreUpdate() {
        // Given
        TenantSubscription subscription = TenantSubscription.builder()
                .tenant(Tenant.builder().id("tenant-id").build())
                .planName("Basic Plan")
                .build();
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(1);
        subscription.setUpdatedAt(originalUpdatedAt);

        // When
        subscription.preUpdate();

        // Then
        assertTrue(subscription.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testTenantSubscriptionValidationSuccess() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantSubscription subscription = TenantSubscription.builder()
                .tenant(tenant)
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(25)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .build();

        // When
        Set<ConstraintViolation<TenantSubscription>> violations = validator.validate(subscription);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTenantSubscriptionValidationTenantRequired() {
        // Given
        TenantSubscription subscription = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(25)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .build();

        // When
        Set<ConstraintViolation<TenantSubscription>> violations = validator.validate(subscription);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tenant")));
    }

    @Test
    void testTenantSubscriptionValidationPlanNameRequired() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantSubscription subscription = TenantSubscription.builder()
                .tenant(tenant)
                .planType(TenantSubscription.PlanType.PREMIUM)
                .maxUsers(25)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .build();

        // When
        Set<ConstraintViolation<TenantSubscription>> violations = validator.validate(subscription);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("planName")));
    }

    @Test
    void testTenantSubscriptionEqualsAndHashCode() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantSubscription subscription1 = TenantSubscription.builder()
                .id("sub-id")
                .tenant(tenant)
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(25)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .build();

        TenantSubscription subscription2 = TenantSubscription.builder()
                .id("sub-id")
                .tenant(tenant)
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(false)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .build();

        TenantSubscription subscription3 = TenantSubscription.builder()
                .id("different-id")
                .tenant(tenant)
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(25)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .build();

        // Then
        assertEquals(subscription1, subscription2);
        assertNotEquals(subscription1, subscription3);
        assertEquals(subscription1.hashCode(), subscription2.hashCode());
        assertNotEquals(subscription1.hashCode(), subscription3.hashCode());
    }

    @Test
    void testTenantSubscriptionPlanTypeEnum() {
        // Given & When & Then
        assertEquals(3, TenantSubscription.PlanType.values().length);
        assertNotNull(TenantSubscription.PlanType.valueOf("BASIC"));
        assertNotNull(TenantSubscription.PlanType.valueOf("PREMIUM"));
        assertNotNull(TenantSubscription.PlanType.valueOf("ENTERPRISE"));
    }

    @Test
    void testTenantSubscriptionBillingCycleEnum() {
        // Given & When & Then
        assertEquals(3, TenantSubscription.BillingCycle.values().length);
        assertNotNull(TenantSubscription.BillingCycle.valueOf("MONTHLY"));
        assertNotNull(TenantSubscription.BillingCycle.valueOf("QUARTERLY"));
        assertNotNull(TenantSubscription.BillingCycle.valueOf("YEARLY"));
    }

    @Test
    void testTenantSubscriptionToString() {
        // Given
        Tenant tenant = Tenant.builder().id("tenant-id").build();
        TenantSubscription subscription = TenantSubscription.builder()
                .id("sub-id")
                .tenant(tenant)
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(25)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .build();

        // When
        String result = subscription.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("sub-id"));
        assertTrue(result.contains("Premium Plan"));
    }
} 