package com.teneocast.tenant.service;

import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantSubscription;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.repository.TenantRepository;
import com.teneocast.tenant.repository.TenantSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantSubscriptionServiceTest {

    @Mock
    private TenantSubscriptionRepository tenantSubscriptionRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantSubscriptionService tenantSubscriptionService;

    private Tenant testTenant;
    private TenantSubscription testSubscription;
    private TenantSubscription createRequest;
    private TenantSubscription updateRequest;

    @BeforeEach
    void setUp() {
        testTenant = Tenant.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        testSubscription = TenantSubscription.builder()
                .id(UUID.randomUUID().toString())
                .tenant(testTenant)
                .planType(TenantSubscription.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        updateRequest = TenantSubscription.builder()
                .planType(TenantSubscription.PlanType.ENTERPRISE)
                .planName("Enterprise Plan")
                .maxUsers(100)
                .maxStorageGb(500)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.YEARLY)
                .nextBillingDate(LocalDateTime.now().plusYears(1))
                .build();
    }

    @Test
    void testSaveSubscription_CreateNew_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());
        when(tenantSubscriptionRepository.save(any(TenantSubscription.class))).thenReturn(testSubscription);

        // When
        TenantSubscription result = tenantSubscriptionService.saveSubscription(testTenant.getId(), createRequest);

        // Then
        assertNotNull(result);
        assertEquals(testSubscription.getId(), result.getId());
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
        verify(tenantSubscriptionRepository).save(any(TenantSubscription.class));
    }

    @Test
    void testSaveSubscription_UpdateExisting_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testSubscription));
        when(tenantSubscriptionRepository.save(any(TenantSubscription.class))).thenReturn(testSubscription);

        // When
        TenantSubscription result = tenantSubscriptionService.saveSubscription(testTenant.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(testSubscription.getId(), result.getId());
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
        verify(tenantSubscriptionRepository).save(any(TenantSubscription.class));
    }

    @Test
    void testSaveSubscription_TenantNotFound() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantSubscriptionService.saveSubscription(testTenant.getId(), createRequest));
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantSubscriptionRepository, never()).save(any(TenantSubscription.class));
    }

    @Test
    void testGetSubscription_Success() {
        // Given
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testSubscription));

        // When
        TenantSubscription result = tenantSubscriptionService.getSubscription(testTenant.getId());

        // Then
        assertNotNull(result);
        assertEquals(testSubscription.getId(), result.getId());
        assertEquals(testSubscription.getPlanType(), result.getPlanType());
        assertEquals(testSubscription.getPlanName(), result.getPlanName());
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
    }

    @Test
    void testGetSubscription_NotFound() {
        // Given
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantSubscriptionService.getSubscription(testTenant.getId()));
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
    }

    @Test
    void testUpdateSubscription_Success() {
        // Given
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testSubscription));
        when(tenantSubscriptionRepository.save(any(TenantSubscription.class))).thenReturn(testSubscription);

        // When
        TenantSubscription result = tenantSubscriptionService.updateSubscription(testTenant.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(testSubscription.getId(), result.getId());
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
        verify(tenantSubscriptionRepository).save(any(TenantSubscription.class));
    }

    @Test
    void testUpdateSubscription_NotFound() {
        // Given
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantSubscriptionService.updateSubscription(testTenant.getId(), updateRequest));
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
        verify(tenantSubscriptionRepository, never()).save(any(TenantSubscription.class));
    }

    @Test
    void testDeleteSubscription_Success() {
        // Given
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.of(testSubscription));

        // When
        tenantSubscriptionService.deleteSubscription(testTenant.getId());

        // Then
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
        verify(tenantSubscriptionRepository).delete(testSubscription);
    }

    @Test
    void testDeleteSubscription_NotFound() {
        // Given
        when(tenantSubscriptionRepository.findByTenantId(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantSubscriptionService.deleteSubscription(testTenant.getId()));
        verify(tenantSubscriptionRepository).findByTenantId(testTenant.getId());
        verify(tenantSubscriptionRepository, never()).delete(any(TenantSubscription.class));
    }

    @Test
    void testGetSubscriptionsByPlanType_Success() {
        // Given
        TenantSubscription.PlanType planType = TenantSubscription.PlanType.PREMIUM;
        when(tenantSubscriptionRepository.findByPlanType(planType)).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByPlanType(planType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findByPlanType(planType);
    }

    @Test
    void testGetSubscriptionsByPlanTypeWithPagination_Success() {
        // Given
        TenantSubscription.PlanType planType = TenantSubscription.PlanType.PREMIUM;
        Pageable pageable = PageRequest.of(0, 10);
        Page<TenantSubscription> subscriptionPage = new PageImpl<>(List.of(testSubscription), pageable, 1);
        when(tenantSubscriptionRepository.findByPlanType(planType, pageable)).thenReturn(subscriptionPage);

        // When
        Page<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByPlanType(planType, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testSubscription.getId(), result.getContent().get(0).getId());
        verify(tenantSubscriptionRepository).findByPlanType(planType, pageable);
    }

    @Test
    void testGetSubscriptionsByBillingCycle_Success() {
        // Given
        TenantSubscription.BillingCycle billingCycle = TenantSubscription.BillingCycle.MONTHLY;
        when(tenantSubscriptionRepository.findByBillingCycle(billingCycle)).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByBillingCycle(billingCycle);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findByBillingCycle(billingCycle);
    }

    @Test
    void testGetActiveSubscriptions_Success() {
        // Given
        when(tenantSubscriptionRepository.findByIsActiveTrue()).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getActiveSubscriptions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findByIsActiveTrue();
    }

    @Test
    void testGetInactiveSubscriptions_Success() {
        // Given
        when(tenantSubscriptionRepository.findByIsActiveFalse()).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getInactiveSubscriptions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findByIsActiveFalse();
    }

    @Test
    void testGetSubscriptionsByMaxUsers_Success() {
        // Given
        Integer maxUsers = 50;
        when(tenantSubscriptionRepository.findByMaxUsers(maxUsers)).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByMaxUsers(maxUsers);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findByMaxUsers(maxUsers);
    }

    @Test
    void testGetSubscriptionsByMaxStorage_Success() {
        // Given
        Integer maxStorageGb = 100;
        when(tenantSubscriptionRepository.findByMaxStorageGb(maxStorageGb)).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByMaxStorage(maxStorageGb);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findByMaxStorageGb(maxStorageGb);
    }

    @Test
    void testGetSubscriptionsExpiringBy_Success() {
        // Given
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);
        when(tenantSubscriptionRepository.findSubscriptionsExpiringBy(expiryDate)).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsExpiringBy(expiryDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findSubscriptionsExpiringBy(expiryDate);
    }

    @Test
    void testGetSubscriptionsByTenantIdExpiringBy_Success() {
        // Given
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);
        when(tenantSubscriptionRepository.findSubscriptionsByTenantIdExpiringBy(testTenant.getId(), expiryDate)).thenReturn(List.of(testSubscription));

        // When
        List<TenantSubscription> result = tenantSubscriptionService.getSubscriptionsByTenantIdExpiringBy(testTenant.getId(), expiryDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubscription.getId(), result.get(0).getId());
        verify(tenantSubscriptionRepository).findSubscriptionsByTenantIdExpiringBy(testTenant.getId(), expiryDate);
    }

    @Test
    void testGetSubscriptionsCountByPlanType_Success() {
        // Given
        TenantSubscription.PlanType planType = TenantSubscription.PlanType.PREMIUM;
        when(tenantSubscriptionRepository.countByPlanType(planType)).thenReturn(5L);

        // When
        long result = tenantSubscriptionService.getSubscriptionsCountByPlanType(planType);

        // Then
        assertEquals(5L, result);
        verify(tenantSubscriptionRepository).countByPlanType(planType);
    }

    @Test
    void testGetSubscriptionsCountByBillingCycle_Success() {
        // Given
        TenantSubscription.BillingCycle billingCycle = TenantSubscription.BillingCycle.MONTHLY;
        when(tenantSubscriptionRepository.countByBillingCycle(billingCycle)).thenReturn(10L);

        // When
        long result = tenantSubscriptionService.getSubscriptionsCountByBillingCycle(billingCycle);

        // Then
        assertEquals(10L, result);
        verify(tenantSubscriptionRepository).countByBillingCycle(billingCycle);
    }

    @Test
    void testGetActiveSubscriptionsCount_Success() {
        // Given
        when(tenantSubscriptionRepository.countByIsActiveTrue()).thenReturn(15L);

        // When
        long result = tenantSubscriptionService.getActiveSubscriptionsCount();

        // Then
        assertEquals(15L, result);
        verify(tenantSubscriptionRepository).countByIsActiveTrue();
    }

    @Test
    void testGetInactiveSubscriptionsCount_Success() {
        // Given
        when(tenantSubscriptionRepository.countByIsActiveFalse()).thenReturn(3L);

        // When
        long result = tenantSubscriptionService.getInactiveSubscriptionsCount();

        // Then
        assertEquals(3L, result);
        verify(tenantSubscriptionRepository).countByIsActiveFalse();
    }

    @Test
    void testExistsByTenantId_Success() {
        // Given
        when(tenantSubscriptionRepository.existsByTenantId(testTenant.getId())).thenReturn(true);

        // When
        boolean result = tenantSubscriptionService.existsByTenantId(testTenant.getId());

        // Then
        assertTrue(result);
        verify(tenantSubscriptionRepository).existsByTenantId(testTenant.getId());
    }

    @Test
    void testExistsByTenantId_NotFound() {
        // Given
        when(tenantSubscriptionRepository.existsByTenantId(testTenant.getId())).thenReturn(false);

        // When
        boolean result = tenantSubscriptionService.existsByTenantId(testTenant.getId());

        // Then
        assertFalse(result);
        verify(tenantSubscriptionRepository).existsByTenantId(testTenant.getId());
    }

    @Test
    void testValidateSubscriptionRequest_EmptyPlanName() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        TenantSubscription invalidRequest = TenantSubscription.builder()
                .planName("") // Invalid: empty
                .build();

        // When & Then
        assertThrows(TenantValidationException.class, () -> 
            tenantSubscriptionService.saveSubscription(testTenant.getId(), invalidRequest));
    }

    @Test
    void testValidateSubscriptionRequest_InvalidMaxUsers() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        TenantSubscription invalidRequest = TenantSubscription.builder()
                .maxUsers(0) // Invalid: < 1
                .build();

        // When & Then
        assertThrows(TenantValidationException.class, () -> 
            tenantSubscriptionService.saveSubscription(testTenant.getId(), invalidRequest));
    }

    @Test
    void testValidateSubscriptionRequest_InvalidMaxStorage() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        TenantSubscription invalidRequest = TenantSubscription.builder()
                .maxStorageGb(0) // Invalid: < 1
                .build();

        // When & Then
        assertThrows(TenantValidationException.class, () -> 
            tenantSubscriptionService.saveSubscription(testTenant.getId(), invalidRequest));
    }

    @Test
    void testValidateSubscriptionRequest_PastBillingDate() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        TenantSubscription invalidRequest = TenantSubscription.builder()
                .nextBillingDate(LocalDateTime.now().minusDays(1)) // Invalid: in the past
                .build();

        // When & Then
        assertThrows(TenantValidationException.class, () -> 
            tenantSubscriptionService.saveSubscription(testTenant.getId(), invalidRequest));
    }
} 