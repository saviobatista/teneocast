package com.teneocast.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.tenant.controller.GlobalExceptionHandler;
import com.teneocast.tenant.dto.CreateSubscriptionRequest;
import com.teneocast.tenant.dto.TenantSubscriptionDto;
import com.teneocast.tenant.dto.UpdateSubscriptionRequest;
import com.teneocast.tenant.entity.TenantSubscription;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantSubscriptionService;
import com.teneocast.tenant.service.TenantValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TenantSubscriptionControllerTest {

    @Mock
    private TenantSubscriptionService tenantSubscriptionService;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantSubscriptionController tenantSubscriptionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String tenantId;
    private TenantSubscription testSubscription;
    private TenantSubscriptionDto testSubscriptionDto;
    private CreateSubscriptionRequest createRequest;
    private UpdateSubscriptionRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tenantSubscriptionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        // Configure ObjectMapper to handle enum serialization
        objectMapper.findAndRegisterModules();

        tenantId = UUID.randomUUID().toString();

        testSubscription = TenantSubscription.builder()
                .id(UUID.randomUUID().toString())
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

        testSubscriptionDto = TenantSubscriptionDto.builder()
                .id(testSubscription.getId())
                .tenantId(tenantId)
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(50)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .nextBillingDate(testSubscription.getNextBillingDate())
                .createdAt(testSubscription.getCreatedAt())
                .updatedAt(testSubscription.getUpdatedAt())
                .build();

        createRequest = CreateSubscriptionRequest.builder()
                .planType(TenantSubscription.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(5)
                .isActive(true)
                .billingCycle(TenantSubscription.BillingCycle.MONTHLY)
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .build();

        updateRequest = UpdateSubscriptionRequest.builder()
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
    void testSaveSubscription_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.saveSubscription(eq(tenantId), any(TenantSubscription.class)))
                .thenReturn(testSubscription);

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/subscriptions", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testSubscription.getId()))
                .andExpect(jsonPath("$.planName").value(testSubscription.getPlanName()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).saveSubscription(eq(tenantId), any(TenantSubscription.class));
    }

    @Test
    void testSaveSubscription_TenantNotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.saveSubscription(eq(tenantId), any(TenantSubscription.class)))
                .thenThrow(new TenantNotFoundException("Tenant not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/subscriptions", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).saveSubscription(eq(tenantId), any(TenantSubscription.class));
    }

    @Test
    void testGetSubscription_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.getSubscription(tenantId)).thenReturn(testSubscription);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubscription.getId()))
                .andExpect(jsonPath("$.planName").value(testSubscription.getPlanName()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).getSubscription(tenantId);
    }

    @Test
    void testGetSubscription_NotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.getSubscription(tenantId))
                .thenThrow(new TenantNotFoundException("Subscription not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions", tenantId))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).getSubscription(tenantId);
    }

    @Test
    void testUpdateSubscription_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.updateSubscription(eq(tenantId), any(TenantSubscription.class)))
                .thenReturn(testSubscription);

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{tenantId}/subscriptions", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubscription.getId()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).updateSubscription(eq(tenantId), any(TenantSubscription.class));
    }

    @Test
    void testUpdateSubscription_NotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.updateSubscription(eq(tenantId), any(TenantSubscription.class)))
                .thenThrow(new TenantNotFoundException("Subscription not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{tenantId}/subscriptions", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).updateSubscription(eq(tenantId), any(TenantSubscription.class));
    }

    @Test
    void testDeleteSubscription_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doNothing().when(tenantSubscriptionService).deleteSubscription(tenantId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}/subscriptions", tenantId))
                .andExpect(status().isNoContent());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).deleteSubscription(tenantId);
    }

    @Test
    void testDeleteSubscription_NotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doThrow(new TenantNotFoundException("Subscription not found"))
                .when(tenantSubscriptionService).deleteSubscription(tenantId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}/subscriptions", tenantId))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).deleteSubscription(tenantId);
    }

    @Test
    void testGetSubscriptionsByPlanType_Success() throws Exception {
        // Given
        TenantSubscription.PlanType planType = TenantSubscription.PlanType.PREMIUM;
        when(tenantSubscriptionService.getSubscriptionsByPlanType(planType))
                .thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/plan-type/{planType}", tenantId, planType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantSubscriptionService).getSubscriptionsByPlanType(planType);
    }

    @Test
    void testGetSubscriptionsByPlanTypeWithPagination_Success() throws Exception {
        // Given
        TenantSubscription.PlanType planType = TenantSubscription.PlanType.PREMIUM;
        Page<TenantSubscription> subscriptionPage = new PageImpl<>(List.of(testSubscription), PageRequest.of(0, 20), 1);
        // validatePagination is void method, no need to mock return value
        when(tenantSubscriptionService.getSubscriptionsByPlanType(eq(planType), any())).thenReturn(subscriptionPage);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/plan-type/{planType}/page", tenantId, planType)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testSubscriptionDto.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(tenantValidationService).validatePagination(0, 20);
        verify(tenantSubscriptionService).getSubscriptionsByPlanType(eq(planType), any());
    }

    @Test
    void testGetSubscriptionsByBillingCycle_Success() throws Exception {
        // Given
        TenantSubscription.BillingCycle billingCycle = TenantSubscription.BillingCycle.MONTHLY;
        when(tenantSubscriptionService.getSubscriptionsByBillingCycle(billingCycle))
                .thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/billing-cycle/{billingCycle}", tenantId, billingCycle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantSubscriptionService).getSubscriptionsByBillingCycle(billingCycle);
    }

    @Test
    void testGetActiveSubscriptions_Success() throws Exception {
        // Given
        when(tenantSubscriptionService.getActiveSubscriptions()).thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/active", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantSubscriptionService).getActiveSubscriptions();
    }

    @Test
    void testGetInactiveSubscriptions_Success() throws Exception {
        // Given
        when(tenantSubscriptionService.getInactiveSubscriptions()).thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/inactive", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantSubscriptionService).getInactiveSubscriptions();
    }

    @Test
    void testGetSubscriptionsByMaxUsers_Success() throws Exception {
        // Given
        Integer maxUsers = 50;
        when(tenantSubscriptionService.getSubscriptionsByMaxUsers(maxUsers))
                .thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/max-users/{maxUsers}", tenantId, maxUsers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantSubscriptionService).getSubscriptionsByMaxUsers(maxUsers);
    }

    @Test
    void testGetSubscriptionsByMaxStorage_Success() throws Exception {
        // Given
        Integer maxStorageGb = 100;
        when(tenantSubscriptionService.getSubscriptionsByMaxStorage(maxStorageGb))
                .thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/max-storage/{maxStorageGb}", tenantId, maxStorageGb))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantSubscriptionService).getSubscriptionsByMaxStorage(maxStorageGb);
    }

    @Test
    void testGetSubscriptionsExpiringBy_Success() throws Exception {
        // Given
        String expiryDate = LocalDateTime.now().plusDays(30).toString();
        when(tenantSubscriptionService.getSubscriptionsExpiringBy(any(LocalDateTime.class)))
                .thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/expiring", tenantId)
                        .param("expiryDate", expiryDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantSubscriptionService).getSubscriptionsExpiringBy(any(LocalDateTime.class));
    }

    @Test
    void testGetSubscriptionsExpiringBy_InvalidDate() throws Exception {
        // Given
        String invalidDate = "invalid-date";

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/expiring", tenantId)
                        .param("expiryDate", invalidDate))
                .andExpect(status().isBadRequest());

        verify(tenantSubscriptionService, never()).getSubscriptionsExpiringBy(any(LocalDateTime.class));
    }

    @Test
    void testGetSubscriptionsByTenantIdExpiringBy_Success() throws Exception {
        // Given
        String expiryDate = LocalDateTime.now().plusDays(30).toString();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.getSubscriptionsByTenantIdExpiringBy(eq(tenantId), any(LocalDateTime.class)))
                .thenReturn(List.of(testSubscription));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/expiring-by-tenant", tenantId)
                        .param("expiryDate", expiryDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testSubscription.getId()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).getSubscriptionsByTenantIdExpiringBy(eq(tenantId), any(LocalDateTime.class));
    }

    @Test
    void testGetSubscriptionsCountByPlanType_Success() throws Exception {
        // Given
        TenantSubscription.PlanType planType = TenantSubscription.PlanType.PREMIUM;
        when(tenantSubscriptionService.getSubscriptionsCountByPlanType(planType)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/count/plan-type/{planType}", tenantId, planType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(tenantSubscriptionService).getSubscriptionsCountByPlanType(planType);
    }

    @Test
    void testGetSubscriptionsCountByBillingCycle_Success() throws Exception {
        // Given
        TenantSubscription.BillingCycle billingCycle = TenantSubscription.BillingCycle.MONTHLY;
        when(tenantSubscriptionService.getSubscriptionsCountByBillingCycle(billingCycle)).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/count/billing-cycle/{billingCycle}", tenantId, billingCycle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10));

        verify(tenantSubscriptionService).getSubscriptionsCountByBillingCycle(billingCycle);
    }

    @Test
    void testGetActiveSubscriptionsCount_Success() throws Exception {
        // Given
        when(tenantSubscriptionService.getActiveSubscriptionsCount()).thenReturn(15L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/count/active", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(15));

        verify(tenantSubscriptionService).getActiveSubscriptionsCount();
    }

    @Test
    void testGetInactiveSubscriptionsCount_Success() throws Exception {
        // Given
        when(tenantSubscriptionService.getInactiveSubscriptionsCount()).thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/count/inactive", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(tenantSubscriptionService).getInactiveSubscriptionsCount();
    }

    @Test
    void testExistsByTenantId_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantSubscriptionService.existsByTenantId(tenantId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/exists", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService).existsByTenantId(tenantId);
    }

    @Test
    void testExistsByTenantId_InvalidId() throws Exception {
        // Given
        doThrow(new TenantValidationException("Invalid tenant ID format"))
                .when(tenantValidationService).validateTenantId(tenantId);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/exists", tenantId))
                .andExpect(status().isBadRequest());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantSubscriptionService, never()).existsByTenantId(anyString());
    }

    @Test
    void testHealth_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/subscriptions/health", tenantId))
                .andExpect(status().isOk())
                .andExpect(content().string("Tenant subscription service is healthy for tenant: " + tenantId));
    }
} 