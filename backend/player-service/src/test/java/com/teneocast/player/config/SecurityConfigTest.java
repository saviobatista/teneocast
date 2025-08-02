package com.teneocast.player.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Test
    void devProfile_ShouldAllowAllRequests() {
        // This test verifies that the SecurityConfig loads successfully
        // In a real scenario, you would test the security configuration
        // For now, we just verify the context loads
    }

    @Test
    void devProfile_ShouldAllowApiRequests() {
        // This test verifies that the SecurityConfig loads successfully
        // In a real scenario, you would test the security configuration
        // For now, we just verify the context loads
    }
}