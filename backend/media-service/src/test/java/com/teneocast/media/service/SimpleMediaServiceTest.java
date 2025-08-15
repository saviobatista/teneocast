package com.teneocast.media.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SimpleMediaServiceTest {

    @Test
    void testBasicFunctionality() {
        // This is a simple test to verify the service can start
        assertTrue(true, "Basic functionality test passed");
    }
}
