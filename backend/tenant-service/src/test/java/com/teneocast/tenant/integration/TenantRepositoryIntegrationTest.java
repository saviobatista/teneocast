package com.teneocast.tenant.integration;

import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantUser;
import com.teneocast.tenant.repository.TenantRepository;
import com.teneocast.tenant.repository.TenantUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class TenantRepositoryIntegrationTest {

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

    @Autowired
    private TenantUserRepository tenantUserRepository;

    @BeforeEach
    void setUp() {
        tenantUserRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void testTenantCRUDOperations() {
        // Create tenant
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .preferences("{\"volume\": 75}")
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);
        assertThat(savedTenant.getId()).isNotNull();
        assertThat(savedTenant.getName()).isEqualTo("Test Tenant");

        // Read tenant
        Optional<Tenant> foundTenant = tenantRepository.findById(savedTenant.getId());
        assertThat(foundTenant).isPresent();
        assertThat(foundTenant.get().getSubdomain()).isEqualTo("test-tenant");

        // Update tenant
        savedTenant.setName("Updated Test Tenant");
        Tenant updatedTenant = tenantRepository.save(savedTenant);
        assertThat(updatedTenant.getName()).isEqualTo("Updated Test Tenant");

        // Delete tenant
        tenantRepository.deleteById(savedTenant.getId());
        Optional<Tenant> deletedTenant = tenantRepository.findById(savedTenant.getId());
        assertThat(deletedTenant).isEmpty();
    }

    @Test
    void testTenantUserCRUDOperations() {
        // Create tenant first
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        // Create tenant user
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .build();

        TenantUser savedUser = tenantUserRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");

        // Read user
        Optional<TenantUser> foundUser = tenantUserRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(TenantUser.UserRole.MASTER);

        // Update user
        savedUser.setEmail("updated@example.com");
        TenantUser updatedUser = tenantUserRepository.save(savedUser);
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

        // Delete user
        tenantUserRepository.deleteById(savedUser.getId());
        Optional<TenantUser> deletedUser = tenantUserRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void testFindBySubdomain() {
        // Create tenant
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        // Find by subdomain
        Optional<Tenant> foundTenant = tenantRepository.findBySubdomain("test-tenant");
        assertThat(foundTenant).isPresent();
        assertThat(foundTenant.get().getId()).isEqualTo(tenant.getId());

        // Test non-existent subdomain
        Optional<Tenant> notFoundTenant = tenantRepository.findBySubdomain("non-existent");
        assertThat(notFoundTenant).isEmpty();
    }

    @Test
    void testFindByStatus() {
        // Create tenants with different statuses
        Tenant activeTenant = tenantRepository.save(Tenant.builder()
                .name("Active Tenant")
                .subdomain("active-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        Tenant inactiveTenant = tenantRepository.save(Tenant.builder()
                .name("Inactive Tenant")
                .subdomain("inactive-tenant")
                .status(Tenant.TenantStatus.INACTIVE)
                .build());

        // Find active tenants
        List<Tenant> activeTenants = tenantRepository.findByStatus(Tenant.TenantStatus.ACTIVE);
        assertThat(activeTenants).hasSize(1);
        assertThat(activeTenants.get(0).getId()).isEqualTo(activeTenant.getId());

        // Find inactive tenants
        List<Tenant> inactiveTenants = tenantRepository.findByStatus(Tenant.TenantStatus.INACTIVE);
        assertThat(inactiveTenants).hasSize(1);
        assertThat(inactiveTenants.get(0).getId()).isEqualTo(inactiveTenant.getId());
    }

    @Test
    void testFindByTenantIdAndEmail() {
        // Create tenant and user
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        TenantUser user = tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .build());

        // Find user by tenant ID and email
        Optional<TenantUser> foundUser = tenantUserRepository.findByTenantIdAndEmail(tenant.getId(), "test@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(user.getId());

        // Test non-existent user
        Optional<TenantUser> notFoundUser = tenantUserRepository.findByTenantIdAndEmail(tenant.getId(), "nonexistent@example.com");
        assertThat(notFoundUser).isEmpty();
    }

    @Test
    void testFindByTenantIdWithPagination() {
        // Create tenant
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        // Create multiple users
        for (int i = 1; i <= 5; i++) {
            tenantUserRepository.save(TenantUser.builder()
                    .tenant(tenant)
                    .email("user" + i + "@example.com")
                    .passwordHash("$2a$10$dummy.hash")
                    .role(TenantUser.UserRole.PRODUCER)
                    .isActive(true)
                    .build());
        }

        // Test pagination
        Page<TenantUser> firstPage = tenantUserRepository.findByTenantId(tenant.getId(), PageRequest.of(0, 3));
        assertThat(firstPage.getContent()).hasSize(3);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);

        Page<TenantUser> secondPage = tenantUserRepository.findByTenantId(tenant.getId(), PageRequest.of(1, 3));
        assertThat(secondPage.getContent()).hasSize(2);
    }

    @Test
    void testFindByTenantIdAndRole() {
        // Create tenant
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        // Create users with different roles
        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("master@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .build());

        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("producer@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.PRODUCER)
                .isActive(true)
                .build());

        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("manager@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.MANAGER)
                .isActive(true)
                .build());

        // Find users by role
        List<TenantUser> masterUsers = tenantUserRepository.findByTenantIdAndRole(tenant.getId(), TenantUser.UserRole.MASTER);
        assertThat(masterUsers).hasSize(1);
        assertThat(masterUsers.get(0).getEmail()).isEqualTo("master@example.com");

        List<TenantUser> producerUsers = tenantUserRepository.findByTenantIdAndRole(tenant.getId(), TenantUser.UserRole.PRODUCER);
        assertThat(producerUsers).hasSize(1);
        assertThat(producerUsers.get(0).getEmail()).isEqualTo("producer@example.com");
    }

    @Test
    void testFindActiveUsersByTenantId() {
        // Create tenant
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        // Create active and inactive users
        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("active@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .build());

        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("inactive@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.PRODUCER)
                .isActive(false)
                .build());

        // Find only active users
        List<TenantUser> activeUsers = tenantUserRepository.findByTenantIdAndIsActive(tenant.getId(), true);
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("active@example.com");
    }

    @Test
    void testCountByTenantId() {
        // Create tenant
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        // Create multiple users
        for (int i = 1; i <= 3; i++) {
            tenantUserRepository.save(TenantUser.builder()
                    .tenant(tenant)
                    .email("user" + i + "@example.com")
                    .passwordHash("$2a$10$dummy.hash")
                    .role(TenantUser.UserRole.PRODUCER)
                    .isActive(true)
                    .build());
        }

        // Count users
        long userCount = tenantUserRepository.countByTenantId(tenant.getId());
        assertThat(userCount).isEqualTo(3);
    }

    @Test
    void testFindByEmail() {
        // Create tenant and user
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        TenantUser user = tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("test@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .build());

        // Find by email
        List<TenantUser> foundUsers = tenantUserRepository.findByEmail("test@example.com");
        assertThat(foundUsers).isNotEmpty();
        assertThat(foundUsers.get(0).getId()).isEqualTo(user.getId());

        // Test non-existent email
        List<TenantUser> notFoundUsers = tenantUserRepository.findByEmail("nonexistent@example.com");
        assertThat(notFoundUsers).isEmpty();
    }

    @Test
    void testFindUsersByTenantIdAndRoleAndEmailPattern() {
        // Create tenant
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        // Create users with different email patterns
        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("john.doe@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.PRODUCER)
                .isActive(true)
                .build());

        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("jane.smith@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.PRODUCER)
                .isActive(true)
                .build());

        tenantUserRepository.save(TenantUser.builder()
                .tenant(tenant)
                .email("bob.wilson@example.com")
                .passwordHash("$2a$10$dummy.hash")
                .role(TenantUser.UserRole.MANAGER)
                .isActive(true)
                .build());

        // Find users by role and email pattern
        List<TenantUser> producerUsers = tenantUserRepository.findUsersByTenantIdAndRoleAndEmailPattern(
                tenant.getId(), TenantUser.UserRole.PRODUCER, "doe");
        assertThat(producerUsers).hasSize(1);
        assertThat(producerUsers.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }
} 