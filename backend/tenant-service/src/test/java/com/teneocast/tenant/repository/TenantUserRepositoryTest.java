package com.teneocast.tenant.repository;

import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantUser;
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
class TenantUserRepositoryTest {

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
    private TenantUserRepository tenantUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant1;
    private Tenant testTenant2;
    private TenantUser testUser1;
    private TenantUser testUser2;
    private TenantUser testUser3;

    @BeforeEach
    void setUp() {
        tenantUserRepository.deleteAll();
        tenantRepository.deleteAll();

        testTenant1 = Tenant.builder()
                .name("Test Tenant 1")
                .subdomain("test-tenant-1")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        testTenant2 = Tenant.builder()
                .name("Test Tenant 2")
                .subdomain("test-tenant-2")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        testTenant1 = tenantRepository.save(testTenant1);
        testTenant2 = tenantRepository.save(testTenant2);

        testUser1 = TenantUser.builder()
                .tenant(testTenant1)
                .email("user1@test.com")
                .passwordHash("hash1")
                .role(TenantUser.UserRole.MASTER)
                .isActive(true)
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .build();

        testUser2 = TenantUser.builder()
                .tenant(testTenant1)
                .email("user2@test.com")
                .passwordHash("hash2")
                .role(TenantUser.UserRole.PRODUCER)
                .isActive(true)
                .lastLoginAt(LocalDateTime.now().minusHours(2))
                .build();

        testUser3 = TenantUser.builder()
                .tenant(testTenant2)
                .email("user3@test.com")
                .passwordHash("hash3")
                .role(TenantUser.UserRole.MANAGER)
                .isActive(false)
                .build();

        tenantUserRepository.saveAll(List.of(testUser1, testUser2, testUser3));
    }

    @Test
    void testFindByTenantIdAndEmail() {
        // When
        Optional<TenantUser> found = tenantUserRepository.findByTenantIdAndEmail(testTenant1.getId(), "user1@test.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("user1@test.com", found.get().getEmail());
        assertEquals(testTenant1.getId(), found.get().getTenant().getId());
    }

    @Test
    void testFindByTenantIdAndEmailNotFound() {
        // When
        Optional<TenantUser> found = tenantUserRepository.findByTenantIdAndEmail(testTenant1.getId(), "nonexistent@test.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByTenantId() {
        // When
        List<TenantUser> users = tenantUserRepository.findByTenantId(testTenant1.getId());

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getTenant().getId().equals(testTenant1.getId())));
    }

    @Test
    void testFindByTenantIdWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<TenantUser> page = tenantUserRepository.findByTenantId(testTenant1.getId(), pageable);

        // Then
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testFindByTenantIdAndRole() {
        // When
        List<TenantUser> users = tenantUserRepository.findByTenantIdAndRole(testTenant1.getId(), TenantUser.UserRole.MASTER);

        // Then
        assertEquals(1, users.size());
        assertEquals(TenantUser.UserRole.MASTER, users.get(0).getRole());
    }

    @Test
    void testFindByTenantIdAndRoleWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<TenantUser> page = tenantUserRepository.findByTenantIdAndRole(testTenant1.getId(), TenantUser.UserRole.MASTER, pageable);

        // Then
        assertEquals(1, page.getContent().size());
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void testFindByTenantIdAndIsActive() {
        // When
        List<TenantUser> activeUsers = tenantUserRepository.findByTenantIdAndIsActive(testTenant1.getId(), true);

        // Then
        assertEquals(2, activeUsers.size());
        assertTrue(activeUsers.stream().allMatch(u -> u.getIsActive()));
    }

    @Test
    void testFindByTenantIdAndIsActiveWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<TenantUser> page = tenantUserRepository.findByTenantIdAndIsActive(testTenant1.getId(), true, pageable);

        // Then
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testFindByEmail() {
        // When
        List<TenantUser> users = tenantUserRepository.findByEmail("user1@test.com");

        // Then
        assertEquals(1, users.size());
        assertEquals("user1@test.com", users.get(0).getEmail());
    }

    @Test
    void testFindByRole() {
        // When
        List<TenantUser> users = tenantUserRepository.findByRole(TenantUser.UserRole.MASTER);

        // Then
        assertEquals(1, users.size());
        assertEquals(TenantUser.UserRole.MASTER, users.get(0).getRole());
    }

    @Test
    void testFindActiveUsersByTenantId() {
        // When
        List<TenantUser> activeUsers = tenantUserRepository.findActiveUsersByTenantId(testTenant1.getId());

        // Then
        assertEquals(2, activeUsers.size());
        assertTrue(activeUsers.stream().allMatch(u -> u.getIsActive()));
    }

    @Test
    void testFindUsersByTenantIdAndEmailPattern() {
        // When
        List<TenantUser> users = tenantUserRepository.findUsersByTenantIdAndEmailPattern(testTenant1.getId(), "user");

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getEmail().contains("user")));
    }

    @Test
    void testFindUsersByTenantIdAndRoleAndEmailPattern() {
        // When
        List<TenantUser> users = tenantUserRepository.findUsersByTenantIdAndRoleAndEmailPattern(testTenant1.getId(), TenantUser.UserRole.MASTER, "user");

        // Then
        assertEquals(1, users.size());
        assertEquals(TenantUser.UserRole.MASTER, users.get(0).getRole());
        assertTrue(users.get(0).getEmail().contains("user"));
    }

    @Test
    void testCountByTenantId() {
        // When
        long count = tenantUserRepository.countByTenantId(testTenant1.getId());

        // Then
        assertEquals(2, count);
    }

    @Test
    void testCountByTenantIdAndRole() {
        // When
        long count = tenantUserRepository.countByTenantIdAndRole(testTenant1.getId(), TenantUser.UserRole.MASTER);

        // Then
        assertEquals(1, count);
    }

    @Test
    void testCountByTenantIdAndIsActive() {
        // When
        long activeCount = tenantUserRepository.countByTenantIdAndIsActive(testTenant1.getId(), true);
        long inactiveCount = tenantUserRepository.countByTenantIdAndIsActive(testTenant1.getId(), false);

        // Then
        assertEquals(2, activeCount);
        assertEquals(0, inactiveCount);
    }

    @Test
    void testFindUsersLoggedInSince() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(2);

        // When
        List<TenantUser> users = tenantUserRepository.findUsersLoggedInSince(since);

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getLastLoginAt().isAfter(since)));
    }

    @Test
    void testFindUsersByTenantIdLoggedInSince() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(2);

        // When
        List<TenantUser> users = tenantUserRepository.findUsersByTenantIdLoggedInSince(testTenant1.getId(), since);

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getTenant().getId().equals(testTenant1.getId())));
    }

    @Test
    void testFindUsersWithNullLastLogin() {
        // Given
        TenantUser userWithNullLogin = TenantUser.builder()
                .tenant(testTenant1)
                .email("null-login@test.com")
                .passwordHash("hash4")
                .role(TenantUser.UserRole.MANAGER)
                .isActive(true)
                .lastLoginAt(null)
                .build();
        tenantUserRepository.save(userWithNullLogin);

        // When
        List<TenantUser> users = tenantUserRepository.findUsersWithNullLastLogin();

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getLastLoginAt() == null));
    }

    @Test
    void testFindUsersByTenantIdWithNullLastLogin() {
        // Given
        TenantUser userWithNullLogin = TenantUser.builder()
                .tenant(testTenant1)
                .email("null-login@test.com")
                .passwordHash("hash4")
                .role(TenantUser.UserRole.MANAGER)
                .isActive(true)
                .lastLoginAt(null)
                .build();
        tenantUserRepository.save(userWithNullLogin);

        // When
        List<TenantUser> users = tenantUserRepository.findUsersByTenantIdWithNullLastLogin(testTenant1.getId());

        // Then
        assertEquals(1, users.size());
        assertNull(users.get(0).getLastLoginAt());
        assertEquals(testTenant1.getId(), users.get(0).getTenant().getId());
    }

    @Test
    void testSaveAndFindById() {
        // Given
        TenantUser newUser = TenantUser.builder()
                .tenant(testTenant1)
                .email("newuser@test.com")
                .passwordHash("newhash")
                .role(TenantUser.UserRole.PRODUCER)
                .isActive(true)
                .build();

        // When
        TenantUser saved = tenantUserRepository.save(newUser);
        Optional<TenantUser> found = tenantUserRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("newuser@test.com", found.get().getEmail());
        assertEquals(TenantUser.UserRole.PRODUCER, found.get().getRole());
    }

    @Test
    void testDeleteById() {
        // Given
        String userId = testUser1.getId();

        // When
        tenantUserRepository.deleteById(userId);
        Optional<TenantUser> found = tenantUserRepository.findById(userId);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        // When
        List<TenantUser> allUsers = tenantUserRepository.findAll();

        // Then
        assertEquals(3, allUsers.size());
    }

    @Test
    void testFindAllWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<TenantUser> page = tenantUserRepository.findAll(pageable);

        // Then
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }
} 