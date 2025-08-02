package com.teneocast.tenant.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TenantStatusTest {

    @Test
    void testTenantStatusValues() {
        // Given & When & Then
        assertEquals(4, TenantStatus.values().length);
        assertNotNull(TenantStatus.valueOf("ACTIVE"));
        assertNotNull(TenantStatus.valueOf("INACTIVE"));
        assertNotNull(TenantStatus.valueOf("SUSPENDED"));
        assertNotNull(TenantStatus.valueOf("PENDING"));
    }

    @Test
    void testTenantStatusEnumValues() {
        // Given & When & Then
        assertEquals(TenantStatus.ACTIVE, TenantStatus.valueOf("ACTIVE"));
        assertEquals(TenantStatus.INACTIVE, TenantStatus.valueOf("INACTIVE"));
        assertEquals(TenantStatus.SUSPENDED, TenantStatus.valueOf("SUSPENDED"));
        assertEquals(TenantStatus.PENDING, TenantStatus.valueOf("PENDING"));
    }

    @Test
    void testTenantStatusToString() {
        // Given & When & Then
        assertEquals("ACTIVE", TenantStatus.ACTIVE.toString());
        assertEquals("INACTIVE", TenantStatus.INACTIVE.toString());
        assertEquals("SUSPENDED", TenantStatus.SUSPENDED.toString());
        assertEquals("PENDING", TenantStatus.PENDING.toString());
    }

    @Test
    void testTenantStatusOrdinal() {
        // Given & When & Then
        assertEquals(0, TenantStatus.ACTIVE.ordinal());
        assertEquals(1, TenantStatus.INACTIVE.ordinal());
        assertEquals(2, TenantStatus.SUSPENDED.ordinal());
        assertEquals(3, TenantStatus.PENDING.ordinal());
    }

    @Test
    void testTenantStatusName() {
        // Given & When & Then
        assertEquals("ACTIVE", TenantStatus.ACTIVE.name());
        assertEquals("INACTIVE", TenantStatus.INACTIVE.name());
        assertEquals("SUSPENDED", TenantStatus.SUSPENDED.name());
        assertEquals("PENDING", TenantStatus.PENDING.name());
    }

    @Test
    void testTenantStatusEquals() {
        // Given & When & Then
        assertEquals(TenantStatus.ACTIVE, TenantStatus.ACTIVE);
        assertEquals(TenantStatus.INACTIVE, TenantStatus.INACTIVE);
        assertEquals(TenantStatus.SUSPENDED, TenantStatus.SUSPENDED);
        assertEquals(TenantStatus.PENDING, TenantStatus.PENDING);
        
        assertNotEquals(TenantStatus.ACTIVE, TenantStatus.INACTIVE);
        assertNotEquals(TenantStatus.ACTIVE, TenantStatus.SUSPENDED);
        assertNotEquals(TenantStatus.ACTIVE, TenantStatus.PENDING);
    }

    @Test
    void testTenantStatusHashCode() {
        // Given & When & Then
        assertEquals(TenantStatus.ACTIVE.hashCode(), TenantStatus.ACTIVE.hashCode());
        assertEquals(TenantStatus.INACTIVE.hashCode(), TenantStatus.INACTIVE.hashCode());
        assertEquals(TenantStatus.SUSPENDED.hashCode(), TenantStatus.SUSPENDED.hashCode());
        assertEquals(TenantStatus.PENDING.hashCode(), TenantStatus.PENDING.hashCode());
    }

    @Test
    void testTenantStatusCompareTo() {
        // Given & When & Then
        assertEquals(0, TenantStatus.ACTIVE.compareTo(TenantStatus.ACTIVE));
        assertEquals(0, TenantStatus.INACTIVE.compareTo(TenantStatus.INACTIVE));
        assertEquals(0, TenantStatus.SUSPENDED.compareTo(TenantStatus.SUSPENDED));
        assertEquals(0, TenantStatus.PENDING.compareTo(TenantStatus.PENDING));
        
        assertTrue(TenantStatus.ACTIVE.compareTo(TenantStatus.INACTIVE) < 0);
        assertTrue(TenantStatus.INACTIVE.compareTo(TenantStatus.SUSPENDED) < 0);
        assertTrue(TenantStatus.SUSPENDED.compareTo(TenantStatus.PENDING) < 0);
        
        assertTrue(TenantStatus.INACTIVE.compareTo(TenantStatus.ACTIVE) > 0);
        assertTrue(TenantStatus.SUSPENDED.compareTo(TenantStatus.INACTIVE) > 0);
        assertTrue(TenantStatus.PENDING.compareTo(TenantStatus.SUSPENDED) > 0);
    }

    @Test
    void testTenantStatusGetDeclaringClass() {
        // Given & When & Then
        assertEquals(TenantStatus.class, TenantStatus.ACTIVE.getDeclaringClass());
        assertEquals(TenantStatus.class, TenantStatus.INACTIVE.getDeclaringClass());
        assertEquals(TenantStatus.class, TenantStatus.SUSPENDED.getDeclaringClass());
        assertEquals(TenantStatus.class, TenantStatus.PENDING.getDeclaringClass());
    }
} 