package com.teneocast.tenant.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TenantSubscriptionDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testTenantSubscriptionDtoBuilder() {
        // Given & When
        LocalDateTime now = LocalDateTime.now();
        TenantSubscriptionDto dto = TenantSubscriptionDto.builder()
                .id("sub123")
                .tenantId("tenant123")
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(100)
                .maxStorageGb(1000)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .nextBillingDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertEquals("sub123", dto.getId());
        assertEquals("tenant123", dto.getTenantId());
        assertEquals(TenantSubscriptionDto.PlanType.PREMIUM, dto.getPlanType());
        assertEquals("Premium Plan", dto.getPlanName());
        assertEquals(100, dto.getMaxUsers());
        assertEquals(1000, dto.getMaxStorageGb());
        assertTrue(dto.getIsActive());
        assertEquals(TenantSubscriptionDto.BillingCycle.MONTHLY, dto.getBillingCycle());
        assertEquals(now, dto.getNextBillingDate());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionDtoDefaultValues() {
        // Given & When
        TenantSubscriptionDto dto = new TenantSubscriptionDto();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getTenantId());
        assertNull(dto.getPlanType());
        assertNull(dto.getPlanName());
        assertNull(dto.getMaxUsers());
        assertNull(dto.getMaxStorageGb());
        assertNull(dto.getIsActive());
        assertNull(dto.getBillingCycle());
        assertNull(dto.getNextBillingDate());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionDtoAllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TenantSubscriptionDto dto = new TenantSubscriptionDto(
                "sub123",
                "tenant123",
                TenantSubscriptionDto.PlanType.PREMIUM,
                "Premium Plan",
                100,
                1000,
                true,
                TenantSubscriptionDto.BillingCycle.MONTHLY,
                now,
                now,
                now
        );

        // Then
        assertEquals("sub123", dto.getId());
        assertEquals("tenant123", dto.getTenantId());
        assertEquals(TenantSubscriptionDto.PlanType.PREMIUM, dto.getPlanType());
        assertEquals("Premium Plan", dto.getPlanName());
        assertEquals(100, dto.getMaxUsers());
        assertEquals(1000, dto.getMaxStorageGb());
        assertTrue(dto.getIsActive());
        assertEquals(TenantSubscriptionDto.BillingCycle.MONTHLY, dto.getBillingCycle());
        assertEquals(now, dto.getNextBillingDate());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testPlanTypeEnum() {
        // Given & When
        TenantSubscriptionDto.PlanType basic = TenantSubscriptionDto.PlanType.BASIC;
        TenantSubscriptionDto.PlanType premium = TenantSubscriptionDto.PlanType.PREMIUM;
        TenantSubscriptionDto.PlanType enterprise = TenantSubscriptionDto.PlanType.ENTERPRISE;

        // Then
        assertEquals("BASIC", basic.name());
        assertEquals("PREMIUM", premium.name());
        assertEquals("ENTERPRISE", enterprise.name());
        assertEquals(0, basic.ordinal());
        assertEquals(1, premium.ordinal());
        assertEquals(2, enterprise.ordinal());
    }

    @Test
    void testBillingCycleEnum() {
        // Given & When
        TenantSubscriptionDto.BillingCycle monthly = TenantSubscriptionDto.BillingCycle.MONTHLY;
        TenantSubscriptionDto.BillingCycle quarterly = TenantSubscriptionDto.BillingCycle.QUARTERLY;
        TenantSubscriptionDto.BillingCycle yearly = TenantSubscriptionDto.BillingCycle.YEARLY;

        // Then
        assertEquals("MONTHLY", monthly.name());
        assertEquals("QUARTERLY", quarterly.name());
        assertEquals("YEARLY", yearly.name());
        assertEquals(0, monthly.ordinal());
        assertEquals(1, quarterly.ordinal());
        assertEquals(2, yearly.ordinal());
    }

    @Test
    void testTenantSubscriptionDtoEqualsAndHashCode() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TenantSubscriptionDto dto1 = TenantSubscriptionDto.builder()
                .id("sub123")
                .tenantId("tenant123")
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(100)
                .maxStorageGb(1000)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .nextBillingDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        TenantSubscriptionDto dto2 = TenantSubscriptionDto.builder()
                .id("sub123")
                .tenantId("tenant123")
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(100)
                .maxStorageGb(1000)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .nextBillingDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        TenantSubscriptionDto dto3 = TenantSubscriptionDto.builder()
                .id("sub456")
                .tenantId("tenant123")
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(100)
                .maxStorageGb(1000)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .nextBillingDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testTenantSubscriptionDtoToString() {
        // Given
        TenantSubscriptionDto dto = TenantSubscriptionDto.builder()
                .id("sub123")
                .tenantId("tenant123")
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(100)
                .maxStorageGb(1000)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .build();

        // When
        String toString = dto.toString();

        // Then
        assertTrue(toString.contains("sub123"));
        assertTrue(toString.contains("tenant123"));
        assertTrue(toString.contains("PREMIUM"));
        assertTrue(toString.contains("Premium Plan"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("1000"));
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("MONTHLY"));
    }

    @Test
    void testTenantSubscriptionDtoJsonSerialization() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        TenantSubscriptionDto dto = TenantSubscriptionDto.builder()
                .id("sub123")
                .tenantId("tenant123")
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(100)
                .maxStorageGb(1000)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .nextBillingDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        TenantSubscriptionDto deserialized = objectMapper.readValue(json, TenantSubscriptionDto.class);

        // Then
        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getTenantId(), deserialized.getTenantId());
        assertEquals(dto.getPlanType(), deserialized.getPlanType());
        assertEquals(dto.getPlanName(), deserialized.getPlanName());
        assertEquals(dto.getMaxUsers(), deserialized.getMaxUsers());
        assertEquals(dto.getMaxStorageGb(), deserialized.getMaxStorageGb());
        assertEquals(dto.getIsActive(), deserialized.getIsActive());
        assertEquals(dto.getBillingCycle(), deserialized.getBillingCycle());
        assertEquals(dto.getNextBillingDate(), deserialized.getNextBillingDate());
        assertEquals(dto.getCreatedAt(), deserialized.getCreatedAt());
        assertEquals(dto.getUpdatedAt(), deserialized.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionDtoJsonDeserialization() throws Exception {
        // Given
        String json = """
                {
                    "id": "sub123",
                    "tenantId": "tenant123",
                    "planType": "PREMIUM",
                    "planName": "Premium Plan",
                    "maxUsers": 100,
                    "maxStorageGb": 1000,
                    "isActive": true,
                    "billingCycle": "MONTHLY",
                    "nextBillingDate": "2024-01-01T12:00:00",
                    "createdAt": "2024-01-01T12:00:00",
                    "updatedAt": "2024-01-01T12:00:00"
                }
                """;

        // When
        TenantSubscriptionDto dto = objectMapper.readValue(json, TenantSubscriptionDto.class);

        // Then
        assertEquals("sub123", dto.getId());
        assertEquals("tenant123", dto.getTenantId());
        assertEquals(TenantSubscriptionDto.PlanType.PREMIUM, dto.getPlanType());
        assertEquals("Premium Plan", dto.getPlanName());
        assertEquals(100, dto.getMaxUsers());
        assertEquals(1000, dto.getMaxStorageGb());
        assertTrue(dto.getIsActive());
        assertEquals(TenantSubscriptionDto.BillingCycle.MONTHLY, dto.getBillingCycle());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), dto.getNextBillingDate());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), dto.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), dto.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionDtoSetters() {
        // Given
        TenantSubscriptionDto dto = new TenantSubscriptionDto();
        LocalDateTime now = LocalDateTime.now();

        // When
        dto.setId("sub123");
        dto.setTenantId("tenant123");
        dto.setPlanType(TenantSubscriptionDto.PlanType.PREMIUM);
        dto.setPlanName("Premium Plan");
        dto.setMaxUsers(100);
        dto.setMaxStorageGb(1000);
        dto.setIsActive(true);
        dto.setBillingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY);
        dto.setNextBillingDate(now);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        // Then
        assertEquals("sub123", dto.getId());
        assertEquals("tenant123", dto.getTenantId());
        assertEquals(TenantSubscriptionDto.PlanType.PREMIUM, dto.getPlanType());
        assertEquals("Premium Plan", dto.getPlanName());
        assertEquals(100, dto.getMaxUsers());
        assertEquals(1000, dto.getMaxStorageGb());
        assertTrue(dto.getIsActive());
        assertEquals(TenantSubscriptionDto.BillingCycle.MONTHLY, dto.getBillingCycle());
        assertEquals(now, dto.getNextBillingDate());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testTenantSubscriptionDtoToBuilder() {
        // Given
        TenantSubscriptionDto original = TenantSubscriptionDto.builder()
                .id("sub123")
                .tenantId("tenant123")
                .planType(TenantSubscriptionDto.PlanType.BASIC)
                .planName("Basic Plan")
                .maxUsers(10)
                .maxStorageGb(100)
                .isActive(true)
                .billingCycle(TenantSubscriptionDto.BillingCycle.MONTHLY)
                .build();

        // When
        TenantSubscriptionDto modified = original.toBuilder()
                .planType(TenantSubscriptionDto.PlanType.PREMIUM)
                .planName("Premium Plan")
                .maxUsers(100)
                .maxStorageGb(1000)
                .build();

        // Then
        assertEquals("sub123", modified.getId());
        assertEquals("tenant123", modified.getTenantId());
        assertEquals(TenantSubscriptionDto.PlanType.PREMIUM, modified.getPlanType());
        assertEquals("Premium Plan", modified.getPlanName());
        assertEquals(100, modified.getMaxUsers());
        assertEquals(1000, modified.getMaxStorageGb());
        assertTrue(modified.getIsActive());
        assertEquals(TenantSubscriptionDto.BillingCycle.MONTHLY, modified.getBillingCycle());
    }

    @Test
    void testTenantSubscriptionDtoWithNullValues() {
        // Given & When
        TenantSubscriptionDto dto = TenantSubscriptionDto.builder()
                .id(null)
                .tenantId(null)
                .planType(null)
                .planName(null)
                .maxUsers(null)
                .maxStorageGb(null)
                .isActive(null)
                .billingCycle(null)
                .nextBillingDate(null)
                .createdAt(null)
                .updatedAt(null)
                .build();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getTenantId());
        assertNull(dto.getPlanType());
        assertNull(dto.getPlanName());
        assertNull(dto.getMaxUsers());
        assertNull(dto.getMaxStorageGb());
        assertNull(dto.getIsActive());
        assertNull(dto.getBillingCycle());
        assertNull(dto.getNextBillingDate());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }
} 