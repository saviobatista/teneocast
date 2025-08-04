package com.teneocast.tenant.integration;

import com.teneocast.tenant.dto.CreateTenantRequest;
import com.teneocast.tenant.dto.TenantDto;
import com.teneocast.tenant.dto.UpdateTenantRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integration-test")
class TenantIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TenantService tenantService;

    @Test
    void testCreateTenant_Success() {
        // Given
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Integration Test Tenant")
                .subdomain("integration-test")
                .build();

        // When
        TenantDto result = tenantService.createTenant(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Integration Test Tenant");
        assertThat(result.getSubdomain()).isEqualTo("integration-test");
        assertThat(result.getStatus()).isEqualTo(TenantDto.TenantStatus.ACTIVE);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void testCreateTenant_DuplicateSubdomain() {
        // Given
        CreateTenantRequest request1 = CreateTenantRequest.builder()
                .name("First Tenant")
                .subdomain("duplicate-test")
                .build();

        CreateTenantRequest request2 = CreateTenantRequest.builder()
                .name("Second Tenant")
                .subdomain("duplicate-test")
                .build();

        // When & Then
        tenantService.createTenant(request1);
        assertThrows(Exception.class, () -> tenantService.createTenant(request2));
    }

    @Test
    void testGetTenantById_Success() {
        // Given
        Tenant tenant = createTestTenant();

        // When
        TenantDto result = tenantService.getTenantById(tenant.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tenant.getId());
        assertThat(result.getName()).isEqualTo(tenant.getName());
    }

    @Test
    void testGetTenantBySubdomain_Success() {
        // Given
        Tenant tenant = createTestTenant("Test Tenant", "test-subdomain");

        // When
        TenantDto result = tenantService.getTenantBySubdomain("test-subdomain");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubdomain()).isEqualTo("test-subdomain");
    }

    @Test
    void testUpdateTenant_Success() {
        // Given
        Tenant tenant = createTestTenant();
        UpdateTenantRequest updateRequest = UpdateTenantRequest.builder()
                .name("Updated Tenant Name")
                .subdomain("updated-subdomain")
                .build();

        // When
        TenantDto result = tenantService.updateTenant(tenant.getId(), updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Tenant Name");
        assertThat(result.getSubdomain()).isEqualTo("updated-subdomain");
    }

    @Test
    void testDeleteTenant_Success() {
        // Given
        Tenant tenant = createTestTenant();

        // When & Then
        assertDoesNotThrow(() -> tenantService.deleteTenant(tenant.getId()));
    }

    @Test
    void testGetAllTenants_Success() {
        // Given
        createTestTenant("Tenant 1", "tenant-1");
        createTestTenant("Tenant 2", "tenant-2");
        createTestTenant("Tenant 3", "tenant-3");

        // When
        Page<TenantDto> result = tenantService.getAllTenants(PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testGetTenantsByStatus_Success() {
        // Given
        createTestTenant("Active Tenant 1", "active-1");
        createTestTenant("Active Tenant 2", "active-2");

        // When
        List<TenantDto> result = tenantService.getTenantsByStatus(Tenant.TenantStatus.ACTIVE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        result.forEach(tenant -> assertThat(tenant.getStatus()).isEqualTo(TenantDto.TenantStatus.ACTIVE));
    }

    @Test
    void testSearchTenantsByName_Success() {
        // Given
        createTestTenant("Search Test Tenant", "search-test");

        // When
        List<TenantDto> result = tenantService.searchTenantsByName("Search Test");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Search Test");
    }

    @Test
    void testGetActiveTenantsCount_Success() {
        // Given
        createTestTenant("Active Tenant 1", "active-count-1");
        createTestTenant("Active Tenant 2", "active-count-2");

        // When
        long count = tenantService.getActiveTenantsCount();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testExistsById_Success() {
        // Given
        Tenant tenant = createTestTenant();

        // When
        boolean exists = tenantService.existsById(tenant.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsBySubdomain_Success() {
        // Given
        createTestTenant("Test Tenant", "exists-test");

        // When
        boolean exists = tenantService.existsBySubdomain("exists-test");

        // Then
        assertThat(exists).isTrue();
    }

    // Note: REST API tests are not included here as they require authentication
    // and are better suited for separate controller integration tests
} 