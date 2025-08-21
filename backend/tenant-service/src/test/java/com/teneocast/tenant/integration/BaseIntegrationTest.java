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

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("ci")
public abstract class BaseIntegrationTest {

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
        
        // Override server context path to prevent conflicts
        registry.add("server.servlet.context-path", () -> "");
        
        // Add database initialization settings
        registry.add("spring.jpa.defer-datasource-initialization", () -> "false");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected TenantUserRepository tenantUserRepository;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DataSource dataSource;

    @LocalServerPort
    protected int port;

    @PostConstruct
    void verifyDatabaseConnection() {
        // Verify database connection and wait for it to be ready
        int maxRetries = 10;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try (Connection connection = dataSource.getConnection()) {
                // Test if we can execute a simple query
                connection.createStatement().execute("SELECT 1");
                System.out.println("Database connection established successfully");
                break;
            } catch (SQLException e) {
                retryCount++;
                System.out.println("Database connection attempt " + retryCount + " failed: " + e.getMessage());
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Failed to establish database connection after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(2000); // Wait 2 seconds before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Database connection verification interrupted", ie);
                }
            }
        }
        
        // Ensure schema is created by triggering a simple repository operation
        try {
            // This will trigger Hibernate to create the schema
            tenantRepository.count();
            System.out.println("Database schema created successfully");
        } catch (Exception e) {
            System.out.println("Warning: Could not verify schema creation: " + e.getMessage());
        }
    }

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