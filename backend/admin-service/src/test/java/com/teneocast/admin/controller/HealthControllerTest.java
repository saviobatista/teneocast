package com.teneocast.admin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    @Test
    void healthEndpoint_ShouldReturnUpStatus() {
        // Given
        HealthController controller = new HealthController();
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.health();
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("admin-service", response.getBody().get("service"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
