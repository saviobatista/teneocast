package com.teneocast.media.controller;

import com.teneocast.media.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthController {
    
    private final DataSource dataSource;
    private final StorageService storageService;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "media-service");
        response.put("timestamp", System.currentTimeMillis());
        
        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            response.put("database", "UP");
        } catch (Exception e) {
            response.put("database", "DOWN");
            response.put("status", "DOWN");
            log.error("Database health check failed", e);
        }
        
        // Check storage connectivity
        try {
            boolean storageHealthy = storageService.fileExists("health-check");
            response.put("storage", storageHealthy ? "UP" : "DOWN");
            if (!storageHealthy) {
                response.put("status", "DOWN");
            }
        } catch (Exception e) {
            response.put("storage", "DOWN");
            response.put("status", "DOWN");
            log.error("Storage health check failed", e);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "media-service");
        response.put("timestamp", System.currentTimeMillis());
        
        // Database health
        Map<String, Object> dbHealth = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            dbHealth.put("status", "UP");
            dbHealth.put("url", connection.getMetaData().getURL());
            dbHealth.put("database", connection.getCatalog());
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        response.put("database", dbHealth);
        
        // Storage health
        Map<String, Object> storageHealth = new HashMap<>();
        try {
            boolean storageHealthy = storageService.fileExists("health-check");
            storageHealth.put("status", storageHealthy ? "UP" : "DOWN");
            storageHealth.put("endpoint", "S3/MinIO");
        } catch (Exception e) {
            storageHealth.put("status", "DOWN");
            storageHealth.put("error", e.getMessage());
        }
        response.put("storage", storageHealth);
        
        // Overall status
        boolean allHealthy = "UP".equals(dbHealth.get("status")) && "UP".equals(storageHealth.get("status"));
        response.put("status", allHealthy ? "UP" : "DOWN");
        
        return ResponseEntity.ok(response);
    }
}
