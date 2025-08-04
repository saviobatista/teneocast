package com.teneocast.tenant.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
class SimpleIntegrationTest extends BaseIntegrationTest {

    @Test
    void testContextLoads() {
        // This test verifies that the Spring context loads successfully
        assertThat(tenantRepository).isNotNull();
        assertThat(tenantUserRepository).isNotNull();
    }

    @Test
    void testDatabaseConnection() {
        // This test verifies that the database connection works
        long count = tenantRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }
} 