package com.teneocast.admin.entity;

import com.teneocast.common.dto.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserTest {

    @Test
    void adminUser_ShouldCreateSuccessfully() {
        // Given
        UUID id = UUID.randomUUID();
        String email = "test@teneocast.com";
        String passwordHash = "hashedPassword";
        UserRole role = UserRole.ROOT;
        
        // When
        AdminUser adminUser = AdminUser.builder()
                .id(id)
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .isActive(true)
                .build();
        
        // Then
        assertNotNull(adminUser);
        assertEquals(id, adminUser.getId());
        assertEquals(email, adminUser.getEmail());
        assertEquals(passwordHash, adminUser.getPasswordHash());
        assertEquals(role, adminUser.getRole());
        assertTrue(adminUser.getIsActive());
    }

    @Test
    void adminUser_ShouldSetTimestampsOnCreate() {
        // Given
        AdminUser adminUser = new AdminUser();
        
        // When
        adminUser.onCreate();
        
        // Then
        assertNotNull(adminUser.getId());
        assertNotNull(adminUser.getCreatedAt());
        assertNotNull(adminUser.getUpdatedAt());
    }

    @Test
    void adminUser_ShouldSetTimestampOnUpdate() {
        // Given
        AdminUser adminUser = AdminUser.builder()
                .email("test@teneocast.com")
                .passwordHash("hash")
                .role(UserRole.OPERATOR)
                .build();
        adminUser.onCreate();
        
        LocalDateTime originalUpdatedAt = adminUser.getUpdatedAt();
        
        // When
        adminUser.onUpdate();
        
        // Then
        assertTrue(adminUser.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}
