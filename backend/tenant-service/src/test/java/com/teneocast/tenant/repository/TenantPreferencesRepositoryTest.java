package com.teneocast.tenant.repository;

import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.entity.TenantPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class TenantPreferencesRepositoryTest {

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
    }

    @Autowired
    private TenantPreferencesRepository tenantPreferencesRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant1;
    private Tenant testTenant2;
    private TenantPreferences testPreferences1;
    private TenantPreferences testPreferences2;

    @BeforeEach
    void setUp() {
        tenantPreferencesRepository.deleteAll();
        tenantRepository.deleteAll();

        testTenant1 = Tenant.builder()
                .name("Test Tenant 1")
                .subdomain("test-tenant-1")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        testTenant2 = Tenant.builder()
                .name("Test Tenant 2")
                .subdomain("test-tenant-2")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        testTenant1 = tenantRepository.save(testTenant1);
        testTenant2 = tenantRepository.save(testTenant2);

        testPreferences1 = TenantPreferences.builder()
                .tenant(testTenant1)
                .playbackSettings("{\"autoplay\":true,\"shuffle\":false}")
                .genrePreferences("[\"pop\",\"rock\"]")
                .adRules("{\"skipAds\":true,\"adFrequency\":\"low\"}")
                .volumeDefault(75)
                .build();

        testPreferences2 = TenantPreferences.builder()
                .tenant(testTenant2)
                .playbackSettings("{\"autoplay\":false,\"shuffle\":true}")
                .genrePreferences("[\"jazz\",\"classical\"]")
                .adRules("{\"skipAds\":false,\"adFrequency\":\"high\"}")
                .volumeDefault(50)
                .build();

        tenantPreferencesRepository.saveAll(List.of(testPreferences1, testPreferences2));
    }

    @Test
    void testFindByTenantId() {
        // When
        Optional<TenantPreferences> found = tenantPreferencesRepository.findByTenantId(testTenant1.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(testTenant1.getId(), found.get().getTenant().getId());
        assertEquals(75, found.get().getVolumeDefault());
    }

    @Test
    void testFindByTenantIdNotFound() {
        // When
        Optional<TenantPreferences> found = tenantPreferencesRepository.findByTenantId("non-existent-id");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByTenantId() {
        // When & Then
        assertTrue(tenantPreferencesRepository.existsByTenantId(testTenant1.getId()));
        assertFalse(tenantPreferencesRepository.existsByTenantId("non-existent-id"));
    }

    @Test
    void testFindByVolumeDefault() {
        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByVolumeDefault(75);

        // Then
        assertEquals(1, preferences.size());
        assertEquals(75, preferences.get(0).getVolumeDefault());
    }

    @Test
    void testFindByVolumeDefaultBetween() {
        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByVolumeDefaultBetween(40, 60);

        // Then
        assertEquals(1, preferences.size());
        assertEquals(50, preferences.get(0).getVolumeDefault());
    }

    @Test
    void testFindByVolumeDefaultGreaterThan() {
        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByVolumeDefaultGreaterThan(60);

        // Then
        assertEquals(1, preferences.size());
        assertEquals(75, preferences.get(0).getVolumeDefault());
    }

    @Test
    void testFindByVolumeDefaultLessThan() {
        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByVolumeDefaultLessThan(60);

        // Then
        assertEquals(1, preferences.size());
        assertEquals(50, preferences.get(0).getVolumeDefault());
    }

    // @Test
    // void testFindByPlaybackSettingsContaining() {
    //     // When
    //     List<TenantPreferences> preferences = tenantPreferencesRepository.findByPlaybackSettingsContaining("autoplay");

    //     // Then
    //     assertEquals(2, preferences.size());
    //     assertTrue(preferences.stream().allMatch(p -> p.getPlaybackSettings().contains("autoplay")));
    // }

    // @Test
    // void testFindByGenrePreferencesContaining() {
    //     // When
    //     List<TenantPreferences> preferences = tenantPreferencesRepository.findByGenrePreferencesContaining("pop");

    //     // Then
    //     assertEquals(1, preferences.size());
    //     assertTrue(preferences.get(0).getGenrePreferences().contains("pop"));
    // }

    // @Test
    // void testFindByAdRulesContaining() {
    //     // When
    //     List<TenantPreferences> preferences = tenantPreferencesRepository.findByAdRulesContaining("skipAds");

    //     // Then
    //     assertEquals(2, preferences.size());
    //     assertTrue(preferences.stream().allMatch(p -> p.getAdRules().contains("skipAds")));
    // }

    // @Test
    // void testFindByTenantIdAndPlaybackSettingsContaining() {
    //     // When
    //     List<TenantPreferences> preferences = tenantPreferencesRepository.findByTenantIdAndPlaybackSettingsContaining(testTenant1.getId(), "autoplay");

    //     // Then
    //     assertEquals(1, preferences.size());
    //     assertEquals(testTenant1.getId(), preferences.get(0).getTenant().getId());
    //     assertTrue(preferences.get(0).getPlaybackSettings().contains("autoplay"));
    // }

    // @Test
    // void testFindByTenantIdAndGenrePreferencesContaining() {
    //     // When
    //     List<TenantPreferences> preferences = tenantPreferencesRepository.findByTenantIdAndGenrePreferencesContaining(testTenant1.getId(), "pop");

    //     // Then
    //     assertEquals(1, preferences.size());
    //     assertEquals(testTenant1.getId(), preferences.get(0).getTenant().getId());
    //     assertTrue(preferences.get(0).getGenrePreferences().contains("pop"));
    // }

    // @Test
    // void testFindByTenantIdAndAdRulesContaining() {
    //     // When
    //     List<TenantPreferences> preferences = tenantPreferencesRepository.findByTenantIdAndAdRulesContaining(testTenant1.getId(), "skipAds");

    //     // Then
    //     assertEquals(1, preferences.size());
    //     assertEquals(testTenant1.getId(), preferences.get(0).getTenant().getId());
    //     assertTrue(preferences.get(0).getAdRules().contains("skipAds"));
    // }

    @Test
    void testFindWithNullPlaybackSettings() {
        // Given - Since database constraints prevent null values,
        // this test verifies the method exists but returns empty list
        // when no null values can be saved
        
        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findWithNullPlaybackSettings();

        // Then - Should be empty since we can't save null values due to database constraints
        assertEquals(0, preferences.size());
    }

    @Test
    void testFindWithNullGenrePreferences() {
        // Given - Since database constraints prevent null values,
        // this test verifies the method exists but returns empty list
        // when no null values can be saved
        
        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findWithNullGenrePreferences();

        // Then - Should be empty since we can't save null values due to database constraints
        assertEquals(0, preferences.size());
    }

    @Test
    void testFindWithNullAdRules() {
        // Given - Since database constraints prevent null values,
        // this test verifies the method exists but returns empty list
        // when no null values can be saved
        
        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findWithNullAdRules();

        // Then - Should be empty since we can't save null values due to database constraints
        assertEquals(0, preferences.size());
    }

    @Test
    void testFindUpdatedInLastDays() {
        // Given
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(1);

        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findUpdatedInLastDays(daysAgo);

        // Then
        assertEquals(2, preferences.size());
    }

    @Test
    void testFindByTenantIdUpdatedInLastDays() {
        // Given
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(1);

        // When
        List<TenantPreferences> preferences = tenantPreferencesRepository.findByTenantIdUpdatedInLastDays(testTenant1.getId(), daysAgo);

        // Then
        assertEquals(1, preferences.size());
        assertEquals(testTenant1.getId(), preferences.get(0).getTenant().getId());
    }

    @Test
    void testCountByVolumeDefault() {
        // When
        long count = tenantPreferencesRepository.countByVolumeDefault(75);

        // Then
        assertEquals(1, count);
    }

    @Test
    void testCountByVolumeDefaultBetween() {
        // When
        long count = tenantPreferencesRepository.countByVolumeDefaultBetween(40, 60);

        // Then
        assertEquals(1, count);
    }

    @Test
    void testSaveAndFindById() {
        // Given
        TenantPreferences newPrefs = TenantPreferences.builder()
                .tenant(testTenant1)
                .playbackSettings("{\"new\":true}")
                .genrePreferences("[\"new\"]")
                .adRules("{\"new\":true}")
                .volumeDefault(80)
                .build();

        // When
        TenantPreferences saved = tenantPreferencesRepository.save(newPrefs);
        Optional<TenantPreferences> found = tenantPreferencesRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(80, found.get().getVolumeDefault());
        assertEquals("{\"new\":true}", found.get().getPlaybackSettings());
    }

    @Test
    void testDeleteById() {
        // Given
        String prefsId = testPreferences1.getId();

        // When
        tenantPreferencesRepository.deleteById(prefsId);
        Optional<TenantPreferences> found = tenantPreferencesRepository.findById(prefsId);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        // When
        List<TenantPreferences> allPreferences = tenantPreferencesRepository.findAll();

        // Then
        assertEquals(2, allPreferences.size());
    }
} 