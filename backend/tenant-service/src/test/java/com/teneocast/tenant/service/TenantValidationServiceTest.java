package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.CreateTenantRequest;
import com.teneocast.tenant.dto.UpdateTenantRequest;
import com.teneocast.tenant.exception.TenantValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantValidationServiceTest {

    private TenantValidationService tenantValidationService;

    @BeforeEach
    void setUp() {
        tenantValidationService = new TenantValidationService();
    }

    @Test
    void testValidateCreateTenantRequest_Success() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        // When & Then
        assertDoesNotThrow(() -> tenantValidationService.validateCreateTenantRequest(request));
    }

    @Test
    void testValidateCreateTenantRequest_NullRequest() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateCreateTenantRequest(null));
    }

    @Test
    void testValidateUpdateTenantRequest_Success() {
        // Given
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .build();

        // When & Then
        assertDoesNotThrow(() -> tenantValidationService.validateUpdateTenantRequest(request));
    }

    @Test
    void testValidateUpdateTenantRequest_NullRequest() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateUpdateTenantRequest(null));
    }

    @Test
    void testValidateTenantName_Success() {
        // Given
        String validName = "Test Tenant Name";

        // When & Then
        assertDoesNotThrow(() -> tenantValidationService.validateTenantName(validName));
    }

    @Test
    void testValidateTenantName_Null() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantName(null));
    }

    @Test
    void testValidateTenantName_Empty() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantName(""));
    }

    @Test
    void testValidateTenantName_TooShort() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantName("A"));
    }

    @Test
    void testValidateTenantName_TooLong() {
        // Given
        String longName = "A".repeat(101);

        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantName(longName));
    }

    @Test
    void testValidateTenantName_InvalidCharacters() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantName("Test@Tenant"));
    }

    @Test
    void testValidateSubdomain_Success() {
        // Given
        String validSubdomain = "test-tenant";

        // When & Then
        assertDoesNotThrow(() -> tenantValidationService.validateSubdomain(validSubdomain));
    }

    @Test
    void testValidateSubdomain_Null() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain(null));
    }

    @Test
    void testValidateSubdomain_Empty() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain(""));
    }

    @Test
    void testValidateSubdomain_TooShort() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain("ab"));
    }

    @Test
    void testValidateSubdomain_TooLong() {
        // Given
        String longSubdomain = "a".repeat(64);

        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain(longSubdomain));
    }

    @Test
    void testValidateSubdomain_InvalidCharacters() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain("test@tenant"));
    }

    @Test
    void testValidateSubdomain_StartsWithHyphen() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain("-test-tenant"));
    }

    @Test
    void testValidateSubdomain_EndsWithHyphen() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain("test-tenant-"));
    }

    @Test
    void testValidateSubdomain_ReservedSubdomain() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain("www"));
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain("api"));
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateSubdomain("admin"));
    }

    @Test
    void testValidateTenantId_Success() {
        // Given
        String validId = "123e4567-e89b-12d3-a456-426614174000";

        // When & Then
        assertDoesNotThrow(() -> tenantValidationService.validateTenantId(validId));
    }

    @Test
    void testValidateTenantId_Null() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantId(null));
    }

    @Test
    void testValidateTenantId_Empty() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantId(""));
    }

    @Test
    void testValidateTenantId_InvalidFormat() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validateTenantId("invalid-uuid"));
    }

    @Test
    void testValidatePagination_Success() {
        // When & Then
        assertDoesNotThrow(() -> tenantValidationService.validatePagination(0, 10));
    }

    @Test
    void testValidatePagination_NegativePage() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validatePagination(-1, 10));
    }

    @Test
    void testValidatePagination_ZeroSize() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validatePagination(0, 0));
    }

    @Test
    void testValidatePagination_TooLargeSize() {
        // When & Then
        assertThrows(TenantValidationException.class, () -> tenantValidationService.validatePagination(0, 101));
    }

    @Test
    void testIsValidEmail_Success() {
        // Given
        String validEmail = "test@example.com";

        // When
        boolean result = tenantValidationService.isValidEmail(validEmail);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsValidEmail_Null() {
        // When
        boolean result = tenantValidationService.isValidEmail(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidEmail_Empty() {
        // When
        boolean result = tenantValidationService.isValidEmail("");

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidEmail_InvalidFormat() {
        // Given
        String invalidEmail = "invalid-email";

        // When
        boolean result = tenantValidationService.isValidEmail(invalidEmail);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidEmail_NoDomain() {
        // Given
        String invalidEmail = "test@";

        // When
        boolean result = tenantValidationService.isValidEmail(invalidEmail);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValidEmail_NoAtSymbol() {
        // Given
        String invalidEmail = "testexample.com";

        // When
        boolean result = tenantValidationService.isValidEmail(invalidEmail);

        // Then
        assertFalse(result);
    }
} 