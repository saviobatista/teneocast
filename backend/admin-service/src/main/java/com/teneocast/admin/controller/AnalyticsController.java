package com.teneocast.admin.controller;

import com.teneocast.admin.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getPlatformOverview() {
        log.info("Fetching platform overview analytics");
        Map<String, Object> overview = analyticsService.getPlatformOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/admin-users")
    public ResponseEntity<Map<String, Object>> getAdminUserAnalytics() {
        log.info("Fetching admin user analytics");
        Map<String, Object> analytics = analyticsService.getAdminUserAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/impersonation")
    public ResponseEntity<Map<String, Object>> getImpersonationAnalytics() {
        log.info("Fetching impersonation analytics");
        Map<String, Object> analytics = analyticsService.getImpersonationAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/platform-settings")
    public ResponseEntity<Map<String, Object>> getPlatformSettingsAnalytics() {
        log.info("Fetching platform settings analytics");
        Map<String, Object> analytics = analyticsService.getPlatformSettingsAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/comprehensive")
    public ResponseEntity<Map<String, Object>> getComprehensiveAnalytics() {
        log.info("Fetching comprehensive platform analytics");
        Map<String, Object> analytics = analyticsService.getComprehensiveAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/custom/{metric}")
    public ResponseEntity<Map<String, Object>> getCustomAnalytics(@PathVariable String metric,
                                                               @RequestParam(required = false) String since) {
        log.info("Fetching custom analytics for metric: {} since: {}", metric, since);
        
        LocalDateTime sinceDateTime = null;
        if (since != null && !since.isEmpty()) {
            try {
                sinceDateTime = LocalDateTime.parse(since);
            } catch (Exception e) {
                log.warn("Invalid date format: {}", since);
                return ResponseEntity.badRequest().build();
            }
        }
        
        Map<String, Object> analytics = analyticsService.getCustomAnalytics(metric, sinceDateTime);
        
        if (analytics.containsKey("error")) {
            return ResponseEntity.badRequest().body(analytics);
        }
        
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        log.info("Fetching system health status");
        Map<String, Object> overview = analyticsService.getPlatformOverview();
        Map<String, Object> health = (Map<String, Object>) overview.get("systemHealth");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getStatsSummary() {
        log.info("Fetching platform statistics summary");
        Map<String, Object> overview = analyticsService.getPlatformOverview();
        
        // Extract key statistics
        Map<String, Object> summary = Map.of(
            "adminUsers", overview.get("adminUsers"),
            "platformSettings", overview.get("platformSettings"),
            "impersonationSessions", overview.get("impersonationSessions"),
            "generatedAt", overview.get("systemHealth")
        );
        
        return ResponseEntity.ok(summary);
    }
}
