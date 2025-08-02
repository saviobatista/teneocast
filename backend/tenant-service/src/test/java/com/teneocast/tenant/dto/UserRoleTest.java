package com.teneocast.tenant.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserRoleTest {

    @Test
    void testUserRoleValues() {
        // Given & When & Then
        assertEquals(3, UserRole.values().length);
        assertNotNull(UserRole.valueOf("MASTER"));
        assertNotNull(UserRole.valueOf("PRODUCER"));
        assertNotNull(UserRole.valueOf("MANAGER"));
    }

    @Test
    void testUserRoleEnumValues() {
        // Given & When & Then
        assertEquals(UserRole.MASTER, UserRole.valueOf("MASTER"));
        assertEquals(UserRole.PRODUCER, UserRole.valueOf("PRODUCER"));
        assertEquals(UserRole.MANAGER, UserRole.valueOf("MANAGER"));
    }

    @Test
    void testUserRoleToString() {
        // Given & When & Then
        assertEquals("MASTER", UserRole.MASTER.toString());
        assertEquals("PRODUCER", UserRole.PRODUCER.toString());
        assertEquals("MANAGER", UserRole.MANAGER.toString());
    }

    @Test
    void testUserRoleOrdinal() {
        // Given & When & Then
        assertEquals(0, UserRole.MASTER.ordinal());
        assertEquals(1, UserRole.PRODUCER.ordinal());
        assertEquals(2, UserRole.MANAGER.ordinal());
    }

    @Test
    void testUserRoleName() {
        // Given & When & Then
        assertEquals("MASTER", UserRole.MASTER.name());
        assertEquals("PRODUCER", UserRole.PRODUCER.name());
        assertEquals("MANAGER", UserRole.MANAGER.name());
    }

    @Test
    void testUserRoleEquals() {
        // Given & When & Then
        assertEquals(UserRole.MASTER, UserRole.MASTER);
        assertEquals(UserRole.PRODUCER, UserRole.PRODUCER);
        assertEquals(UserRole.MANAGER, UserRole.MANAGER);
        
        assertNotEquals(UserRole.MASTER, UserRole.PRODUCER);
        assertNotEquals(UserRole.MASTER, UserRole.MANAGER);
        assertNotEquals(UserRole.PRODUCER, UserRole.MANAGER);
    }

    @Test
    void testUserRoleHashCode() {
        // Given & When & Then
        assertEquals(UserRole.MASTER.hashCode(), UserRole.MASTER.hashCode());
        assertEquals(UserRole.PRODUCER.hashCode(), UserRole.PRODUCER.hashCode());
        assertEquals(UserRole.MANAGER.hashCode(), UserRole.MANAGER.hashCode());
    }

    @Test
    void testUserRoleCompareTo() {
        // Given & When & Then
        assertEquals(0, UserRole.MASTER.compareTo(UserRole.MASTER));
        assertEquals(0, UserRole.PRODUCER.compareTo(UserRole.PRODUCER));
        assertEquals(0, UserRole.MANAGER.compareTo(UserRole.MANAGER));
        
        assertTrue(UserRole.MASTER.compareTo(UserRole.PRODUCER) < 0);
        assertTrue(UserRole.PRODUCER.compareTo(UserRole.MANAGER) < 0);
        
        assertTrue(UserRole.PRODUCER.compareTo(UserRole.MASTER) > 0);
        assertTrue(UserRole.MANAGER.compareTo(UserRole.PRODUCER) > 0);
    }

    @Test
    void testUserRoleGetDeclaringClass() {
        // Given & When & Then
        assertEquals(UserRole.class, UserRole.MASTER.getDeclaringClass());
        assertEquals(UserRole.class, UserRole.PRODUCER.getDeclaringClass());
        assertEquals(UserRole.class, UserRole.MANAGER.getDeclaringClass());
    }

    @Test
    void testUserRoleValueOfCaseSensitive() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("master"));
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("producer"));
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("manager"));
    }

    @Test
    void testUserRoleValueOfInvalid() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("ADMIN"));
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("USER"));
    }
} 