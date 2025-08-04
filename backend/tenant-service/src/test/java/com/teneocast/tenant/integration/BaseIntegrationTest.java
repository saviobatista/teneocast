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

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        
        // Add connection pool settings for better stability
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "600000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "1800000");
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