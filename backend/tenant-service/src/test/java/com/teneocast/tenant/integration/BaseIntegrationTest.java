package com.teneocast.tenant.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantUser;
import com.teneocast.tenant.repository.TenantRepository;
import com.teneocast.tenant.repository.TenantUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Testcontainers
public abstract class BaseIntegrationTest {

    // Check if we're running in CI (GitHub Actions)
    private static final boolean IS_CI = System.getenv("CI") != null;
    private static final boolean IS_GITHUB_ACTIONS = System.getenv("GITHUB_ACTIONS") != null;

    @Container
    static PostgreSQLContainer<?> postgres = IS_CI || IS_GITHUB_ACTIONS ? null : 
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = IS_CI || IS_GITHUB_ACTIONS ? null :
        new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (IS_CI || IS_GITHUB_ACTIONS) {
            // Use CI services (GitHub Actions provides PostgreSQL and Redis)
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/teneocast_test");
            registry.add("spring.datasource.username", () -> "test");
            registry.add("spring.datasource.password", () -> "test");
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.data.redis.host", () -> "localhost");
            registry.add("spring.data.redis.port", () -> 6379);
        } else {
            // Use Testcontainers for local development
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.data.redis.host", redis::getHost);
            registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        }
        
        // Common configuration for both environments
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
        
        // Override server context path to prevent conflicts
        registry.add("server.servlet.context-path", () -> "");
    }

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected TenantUserRepository tenantUserRepository;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        // Clean up data before each test
        tenantUserRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    protected Tenant createTestTenant() {
        return createTestTenant("Test Tenant", "test-tenant");
    }

    protected Tenant createTestTenant(String name, String subdomain) {
        Tenant tenant = Tenant.builder()
                .name(name)
                .subdomain(subdomain)
                .status(Tenant.TenantStatus.ACTIVE)
                .build();
        return tenantRepository.save(tenant);
    }

    protected TenantUser createTestUser(Tenant tenant) {
        return createTestUser(tenant, "test@test.com", TenantUser.UserRole.MASTER);
    }

    protected TenantUser createTestUser(Tenant tenant, String email, TenantUser.UserRole role) {
        TenantUser user = TenantUser.builder()
                .tenant(tenant)
                .email(email)
                .passwordHash("$2a$10$dummy.hash.for.testing")
                .role(role)
                .isActive(true)
                .build();
        return tenantUserRepository.save(user);
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port + "/tenant";
    }
} 