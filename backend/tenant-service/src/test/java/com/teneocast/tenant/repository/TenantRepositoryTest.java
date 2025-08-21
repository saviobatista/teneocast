package com.teneocast.tenant.repository;

import com.teneocast.tenant.entity.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class TenantRepositoryTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Always use external services (CI services or local services)
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/teneocast_test");
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> 6379);
        
        // Common configuration
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        
        // Add connection pool settings for better stability
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "600000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "1800000");
        registry.add("spring.datasource.hikari.auto-commit", () -> "false");
        
        // Disable autocommit to fix transaction issues
        registry.add("spring.jpa.properties.hibernate.connection.provider_disables_autocommit", () -> "true");
        
        // Ensure Flyway is disabled for tests
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant1;
    private Tenant testTenant2;
    private Tenant testTenant3;

    @BeforeEach
    void setUp() {
        tenantRepository.deleteAll();

        testTenant1 = Tenant.builder()
                .name("Test Tenant 1")
                .subdomain("test-tenant-1")
                .status(Tenant.TenantStatus.ACTIVE)
                .preferences("{\"volume\":50}")
                .build();

        testTenant2 = Tenant.builder()
                .name("Test Tenant 2")
                .subdomain("test-tenant-2")
                .status(Tenant.TenantStatus.INACTIVE)
                .preferences("{\"volume\":75}")
                .build();

        testTenant3 = Tenant.builder()
                .name("Another Tenant")
                .subdomain("another-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .preferences("{\"volume\":25}")
                .build();

        tenantRepository.saveAll(List.of(testTenant1, testTenant2, testTenant3));
    }

    @Test
    void testFindBySubdomain() {
        // When
        Optional<Tenant> found = tenantRepository.findBySubdomain("test-tenant-1");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test-tenant-1", found.get().getSubdomain());
    }

    @Test
    void testFindBySubdomainNotFound() {
        // When
        Optional<Tenant> found = tenantRepository.findBySubdomain("non-existent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsBySubdomain() {
        // When & Then
        assertTrue(tenantRepository.existsBySubdomain("test-tenant-1"));
        assertFalse(tenantRepository.existsBySubdomain("non-existent"));
    }

    @Test
    void testFindByStatus() {
        // When
        List<Tenant> activeTenants = tenantRepository.findByStatus(Tenant.TenantStatus.ACTIVE);

        // Then
        assertEquals(2, activeTenants.size());
        assertTrue(activeTenants.stream().allMatch(t -> t.getStatus() == Tenant.TenantStatus.ACTIVE));
    }

    @Test
    void testFindByStatusWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Tenant> page = tenantRepository.findByStatus(Tenant.TenantStatus.ACTIVE, pageable);

        // Then
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().get(0).getStatus() == Tenant.TenantStatus.ACTIVE);
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        // When
        List<Tenant> tenants = tenantRepository.findByNameContainingIgnoreCase("test");

        // Then
        assertEquals(2, tenants.size());
        assertTrue(tenants.stream().allMatch(t -> t.getName().toLowerCase().contains("test")));
    }

    @Test
    void testFindByNameContainingIgnoreCaseWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Tenant> page = tenantRepository.findByNameContainingIgnoreCase("test", pageable);

        // Then
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testFindByStatusAndNameContainingIgnoreCase() {
        // When
        List<Tenant> tenants = tenantRepository.findByStatusAndNameContainingIgnoreCase(Tenant.TenantStatus.ACTIVE, "test");

        // Then
        assertEquals(1, tenants.size());
        assertEquals(Tenant.TenantStatus.ACTIVE, tenants.get(0).getStatus());
        assertTrue(tenants.get(0).getName().toLowerCase().contains("test"));
    }

    @Test
    void testFindByStatusAndNameContainingIgnoreCaseWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Tenant> page = tenantRepository.findByStatusAndNameContainingIgnoreCase(Tenant.TenantStatus.ACTIVE, "test", pageable);

        // Then
        assertEquals(1, page.getContent().size());
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void testCountByStatus() {
        // When
        long activeCount = tenantRepository.countByStatus(Tenant.TenantStatus.ACTIVE);
        long inactiveCount = tenantRepository.countByStatus(Tenant.TenantStatus.INACTIVE);

        // Then
        assertEquals(2, activeCount);
        assertEquals(1, inactiveCount);
    }

    @Test
    void testFindActiveTenants() {
        // When
        List<Tenant> activeTenants = tenantRepository.findActiveTenants();

        // Then
        assertEquals(2, activeTenants.size());
        assertTrue(activeTenants.stream().allMatch(t -> t.getStatus() == Tenant.TenantStatus.ACTIVE));
    }

    @Test
    void testFindTenantsCreatedInLastDays() {
        // Given
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(1);

        // When
        List<Tenant> tenants = tenantRepository.findTenantsCreatedInLastDays(daysAgo);

        // Then
        assertEquals(3, tenants.size());
    }

    @Test
    void testFindTenantsByPreference() {
        // When
        List<Tenant> tenants = tenantRepository.findTenantsByPreference("volume");

        // Then
        assertEquals(3, tenants.size());
        assertTrue(tenants.stream().allMatch(t -> t.getPreferences().contains("volume")));
    }

    @Test
    void testFindTenantsBySubdomainPattern() {
        // When
        List<Tenant> tenants = tenantRepository.findTenantsBySubdomainPattern("test");

        // Then
        assertEquals(2, tenants.size());
        assertTrue(tenants.stream().allMatch(t -> t.getSubdomain().contains("test")));
    }

    @Test
    void testFindTenantsWithNullPreferences() {
        // Given
        Tenant tenantWithNullPrefs = Tenant.builder()
                .name("Null Prefs Tenant")
                .subdomain("null-prefs-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .preferences(null)
                .build();
        tenantRepository.save(tenantWithNullPrefs);

        // When
        List<Tenant> tenants = tenantRepository.findTenantsWithNullPreferences();

        // Then
        assertEquals(1, tenants.size());
        assertNull(tenants.get(0).getPreferences());
    }

    @Test
    void testFindTenantsUpdatedInLastDays() {
        // Given
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(1);

        // When
        List<Tenant> tenants = tenantRepository.findTenantsUpdatedInLastDays(daysAgo);

        // Then
        assertEquals(3, tenants.size());
    }

    @Test
    void testSaveAndFindById() {
        // Given
        Tenant newTenant = Tenant.builder()
                .name("New Tenant")
                .subdomain("new-tenant")
                .status(Tenant.TenantStatus.PENDING)
                .build();

        // When
        Tenant saved = tenantRepository.save(newTenant);
        Optional<Tenant> found = tenantRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("New Tenant", found.get().getName());
        assertEquals("new-tenant", found.get().getSubdomain());
        assertEquals(Tenant.TenantStatus.PENDING, found.get().getStatus());
    }

    @Test
    void testDeleteById() {
        // Given
        String tenantId = testTenant1.getId();

        // When
        tenantRepository.deleteById(tenantId);
        Optional<Tenant> found = tenantRepository.findById(tenantId);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        // When
        List<Tenant> allTenants = tenantRepository.findAll();

        // Then
        assertEquals(3, allTenants.size());
    }

    @Test
    void testFindAllWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Tenant> page = tenantRepository.findAll(pageable);

        // Then
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }
} 