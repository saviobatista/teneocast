package com.teneocast.admin.service;

import com.teneocast.admin.repository.AdminUserRepository;
import com.teneocast.admin.repository.ImpersonationSessionRepository;
import com.teneocast.admin.repository.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AdminUserRepository adminUserRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final ImpersonationSessionRepository impersonationSessionRepository;

    public Map<String, Object> getPlatformOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // Admin user statistics
        long totalAdminUsers = adminUserRepository.count();
        long activeAdminUsers = adminUserRepository.countActiveUsers();
        
        overview.put("adminUsers", Map.of(
            "total", totalAdminUsers,
            "active", activeAdminUsers,
            "inactive", totalAdminUsers - activeAdminUsers
        ));

        // Platform settings statistics
        long totalSettings = platformSettingsRepository.countSettings();
        overview.put("platformSettings", Map.of(
            "total", totalSettings
        ));

        // Impersonation session statistics
        long activeSessions = impersonationSessionRepository.findByIsActiveTrue().size();
        long recentSessions = impersonationSessionRepository.findSessionsStartedSince(
            LocalDateTime.now().minusDays(7)).size();
        
        overview.put("impersonationSessions", Map.of(
            "active", activeSessions,
            "recentWeek", recentSessions
        ));

        // System health indicators
        overview.put("systemHealth", Map.of(
            "status", "HEALTHY",
            "lastUpdated", LocalDateTime.now()
        ));

        return overview;
    }

    public Map<String, Object> getAdminUserAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Role distribution
        long rootUsers = adminUserRepository.findByRole(com.teneocast.common.dto.UserRole.ROOT).size();
        long operatorUsers = adminUserRepository.findByRole(com.teneocast.common.dto.UserRole.OPERATOR).size();
        
        analytics.put("roleDistribution", Map.of(
            "ROOT", rootUsers,
            "OPERATOR", operatorUsers
        ));

        // Activity metrics
        long recentlyActiveUsers = adminUserRepository.findRecentlyActiveUsers().size();
        analytics.put("activityMetrics", Map.of(
            "recentlyActive", recentlyActiveUsers,
            "totalActive", adminUserRepository.countActiveUsers()
        ));

        return analytics;
    }

    public Map<String, Object> getImpersonationAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Session statistics
        long totalActiveSessions = impersonationSessionRepository.findByIsActiveTrue().size();
        long sessionsLast24Hours = impersonationSessionRepository.findSessionsStartedSince(
            LocalDateTime.now().minusHours(24)).size();
        long sessionsLastWeek = impersonationSessionRepository.findSessionsStartedSince(
            LocalDateTime.now().minusDays(7)).size();
        
        analytics.put("sessionMetrics", Map.of(
            "active", totalActiveSessions,
            "last24Hours", sessionsLast24Hours,
            "lastWeek", sessionsLastWeek
        ));

        // Top admin users by session count
        // This would require a more complex query, but for now we'll provide the basic structure
        analytics.put("topAdminUsers", "Feature to be implemented");

        return analytics;
    }

    public Map<String, Object> getPlatformSettingsAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Settings by type
        long stringSettings = platformSettingsRepository.findBySettingType(
            com.teneocast.admin.entity.PlatformSettings.SettingType.STRING).size();
        long integerSettings = platformSettingsRepository.findBySettingType(
            com.teneocast.admin.entity.PlatformSettings.SettingType.INTEGER).size();
        long booleanSettings = platformSettingsRepository.findBySettingType(
            com.teneocast.admin.entity.PlatformSettings.SettingType.BOOLEAN).size();
        long jsonSettings = platformSettingsRepository.findBySettingType(
            com.teneocast.admin.entity.PlatformSettings.SettingType.JSON).size();
        
        analytics.put("settingsByType", Map.of(
            "STRING", stringSettings,
            "INTEGER", integerSettings,
            "BOOLEAN", booleanSettings,
            "JSON", jsonSettings
        ));

        // Recently updated settings
        long recentlyUpdated = platformSettingsRepository.findRecentlyUpdatedSettings().size();
        analytics.put("recentActivity", Map.of(
            "recentlyUpdated", recentlyUpdated
        ));

        return analytics;
    }

    public Map<String, Object> getComprehensiveAnalytics() {
        Map<String, Object> comprehensive = new HashMap<>();
        
        comprehensive.put("platformOverview", getPlatformOverview());
        comprehensive.put("adminUserAnalytics", getAdminUserAnalytics());
        comprehensive.put("impersonationAnalytics", getImpersonationAnalytics());
        comprehensive.put("platformSettingsAnalytics", getPlatformSettingsAnalytics());
        comprehensive.put("generatedAt", LocalDateTime.now());
        
        return comprehensive;
    }

    public Map<String, Object> getCustomAnalytics(String metric, LocalDateTime since) {
        Map<String, Object> custom = new HashMap<>();
        
        switch (metric.toLowerCase()) {
            case "admin_users":
                custom.put("metric", "admin_users");
                custom.put("since", since);
                custom.put("data", getAdminUserAnalytics());
                break;
                
            case "impersonation_sessions":
                custom.put("metric", "impersonation_sessions");
                custom.put("since", since);
                custom.put("data", getImpersonationAnalytics());
                break;
                
            case "platform_settings":
                custom.put("metric", "platform_settings");
                custom.put("since", since);
                custom.put("data", getPlatformSettingsAnalytics());
                break;
                
            default:
                custom.put("error", "Unknown metric: " + metric);
                custom.put("availableMetrics", new String[]{"admin_users", "impersonation_sessions", "platform_settings"});
        }
        
        return custom;
    }
}
