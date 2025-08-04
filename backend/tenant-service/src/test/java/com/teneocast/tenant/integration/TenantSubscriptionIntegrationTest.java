package com.teneocast.tenant.integration;

import com.teneocast.tenant.dto.CreateSubscriptionRequest;
import com.teneocast.tenant.dto.UpdateSubscriptionRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantSubscription;
import com.teneocast.tenant.service.TenantSubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ActiveProfiles("integration-test")
class TenantSubscriptionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TenantSubscriptionService tenantSubscriptionService;

    @Test
    void testSaveSubscription_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantSubscription request = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        // When
        TenantSubscription result = tenantSubscriptionService.saveSubscription(tenant.getId(), request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenant().getId()).isEqualTo(tenant.getId());
        assertThat(result.getPlanType()).isEqualTo(TenantSubscription.PlanType.PREMIUM);
        assertThat(result.getPlanName()).isEqualTo("Premium Plan");
        assertThat(result.getMaxUsers()).isEqualTo(50);
        assertThat(result.getMaxStorageGb()).isEqualTo(100);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getBillingCycle()).isEqualTo(TenantSubscription.BillingCycle.MONTHLY);
    }

    @Test
    void testGetSubscription_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantSubscription request = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();
        tenantSubscriptionService.saveSubscription(tenant.getId(), request);

        // When
        TenantSubscription result = tenantSubscriptionService.getSubscription(tenant.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenant().getId()).isEqualTo(tenant.getId());
        assertThat(result.getPlanType()).isEqualTo(TenantSubscription.PlanType.BASIC);
        assertThat(result.getPlanName()).isEqualTo("Basic Plan");
        assertThat(result.getMaxUsers()).isEqualTo(10);
        assertThat(result.getMaxStorageGb()).isEqualTo(5);
    }

    @Test
    void testUpdateSubscription_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantSubscription initialRequest = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();
        tenantSubscriptionService.saveSubscription(tenant.getId(), initialRequest);

        TenantSubscription updateRequest = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        // When
        TenantSubscription result = tenantSubscriptionService.updateSubscription(tenant.getId(), updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenant().getId()).isEqualTo(tenant.getId());
        assertThat(result.getPlanType()).isEqualTo(TenantSubscription.PlanType.ENTERPRISE);
        assertThat(result.getPlanName()).isEqualTo("Enterprise Plan");
        assertThat(result.getMaxUsers()).isEqualTo(100);
        assertThat(result.getMaxStorageGb()).isEqualTo(500);
        assertThat(result.getBillingCycle()).isEqualTo(TenantSubscription.BillingCycle.YEARLY);
    }

    @Test
    void testDeleteSubscription_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantSubscription request = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();
        tenantSubscriptionService.saveSubscription(tenant.getId(), request);

        // When & Then
        assertDoesNotThrow(() -> tenantSubscriptionService.deleteSubscription(tenant.getId()));
    }

    @Test
    void testGetSubscriptionsByPlanType_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(15)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.QUARTERLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(3))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByPlanType(TenantSubscription.PlanType.BASIC);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        result.forEach(sub -> assertThat(sub.getPlanType()).isEqualTo(TenantSubscription.PlanType.BASIC));
    }

    @Test
    void testGetSubscriptionsByPlanTypeWithPagination_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(15)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.QUARTERLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(3))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(20)
                .maxStorageGb(15)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        Page<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByPlanType(TenantSubscription.PlanType.BASIC, PageRequest.of(0, 2));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testGetSubscriptionsByBillingCycle_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByBillingCycle(TenantSubscription.BillingCycle.MONTHLY);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        result.forEach(sub -> assertThat(sub.getBillingCycle()).isEqualTo(TenantSubscription.BillingCycle.MONTHLY));
    }

    @Test
    void testGetActiveSubscriptions_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(false)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getActiveSubscriptions();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        result.forEach(sub -> assertThat(sub.getIsActive()).isTrue());
    }

    @Test
    void testGetInactiveSubscriptions_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(false)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(false)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getInactiveSubscriptions();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        result.forEach(sub -> assertThat(sub.getIsActive()).isFalse());
    }

    @Test
    void testGetSubscriptionsByMaxUsers_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByMaxUsers(50);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaxUsers()).isEqualTo(50);
    }

    @Test
    void testGetSubscriptionsByMaxStorage_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByMaxStorage(100);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaxStorageGb()).isEqualTo(100);
    }

    @Test
    void testGetSubscriptionsExpiringBy_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusDays(5))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusDays(10))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsExpiringBy(LocalDateTime.now().plusDays(7));

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNextBillingDate()).isBefore(LocalDateTime.now().plusDays(7));
    }

    @Test
    void testGetSubscriptionsCountByPlanType_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(15)
                .maxStorageGb(10)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.QUARTERLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(3))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        long count = tenantSubscriptionService.getSubscriptionsCountByPlanType(TenantSubscription.PlanType.BASIC);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testGetActiveSubscriptionsCount_Success() {
        // Given
        Tenant tenant1 = createTestTenant("Tenant 1", "tenant-1");
        Tenant tenant2 = createTestTenant("Tenant 2", "tenant-2");
        Tenant tenant3 = createTestTenant("Tenant 3", "tenant-3");

        TenantSubscription request1 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request2 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        TenantSubscription request3 = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(false)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();

        tenantSubscriptionService.saveSubscription(tenant1.getId(), request1);
        tenantSubscriptionService.saveSubscription(tenant2.getId(), request2);
        tenantSubscriptionService.saveSubscription(tenant3.getId(), request3);

        // When
        long count = tenantSubscriptionService.getActiveSubscriptionsCount();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testExistsByTenantId_Success() {
        // Given
        Tenant tenant = createTestTenant();
        TenantSubscription request = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();
        tenantSubscriptionService.saveSubscription(tenant.getId(), request);

        // When
        boolean exists = tenantSubscriptionService.existsByTenantId(tenant.getId());

        // Then
        assertThat(exists).isTrue();
    }
} 