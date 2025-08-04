package com.teneocast.tenant.repository;

import com.teneocast.tenant.entity.TenantPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantPreferencesRepository extends JpaRepository<TenantPreferences, String> {

    /**
     * Find preferences by tenant ID
     */
    Optional<TenantPreferences> findByTenantId(String tenantId);

    /**
     * Check if preferences exist by tenant ID
     */
    boolean existsByTenantId(String tenantId);

    /**
     * Find preferences by volume default
     */
    List<TenantPreferences> findByVolumeDefault(Integer volumeDefault);

    /**
     * Find preferences by volume default range
     */
    List<TenantPreferences> findByVolumeDefaultBetween(Integer minVolume, Integer maxVolume);

    /**
     * Find preferences by volume default greater than
     */
    List<TenantPreferences> findByVolumeDefaultGreaterThan(Integer volume);

    /**
     * Find preferences by volume default less than
     */
    List<TenantPreferences> findByVolumeDefaultLessThan(Integer volume);

    /**
     * Find preferences with specific playback settings
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.playbackSettings LIKE %:setting%")
    List<TenantPreferences> findByPlaybackSettingsContaining(@Param("setting") String setting);

    /**
     * Find preferences with specific genre preferences
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.genrePreferences LIKE %:genre%")
    List<TenantPreferences> findByGenrePreferencesContaining(@Param("genre") String genre);

    /**
     * Find preferences with specific ad rules
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.adRules LIKE %:rule%")
    List<TenantPreferences> findByAdRulesContaining(@Param("rule") String rule);

    /**
     * Find preferences by tenant ID with specific playback settings
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.tenant.id = :tenantId AND p.playbackSettings LIKE %:setting%")
    List<TenantPreferences> findByTenantIdAndPlaybackSettingsContaining(@Param("tenantId") String tenantId, @Param("setting") String setting);

    /**
     * Find preferences by tenant ID with specific genre preferences
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.tenant.id = :tenantId AND p.genrePreferences LIKE %:genre%")
    List<TenantPreferences> findByTenantIdAndGenrePreferencesContaining(@Param("tenantId") String tenantId, @Param("genre") String genre);

    /**
     * Find preferences by tenant ID with specific ad rules
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.tenant.id = :tenantId AND p.adRules LIKE %:rule%")
    List<TenantPreferences> findByTenantIdAndAdRulesContaining(@Param("tenantId") String tenantId, @Param("rule") String rule);

    /**
     * Find preferences with null playback settings
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.playbackSettings IS NULL")
    List<TenantPreferences> findWithNullPlaybackSettings();

    /**
     * Find preferences with null genre preferences
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.genrePreferences IS NULL")
    List<TenantPreferences> findWithNullGenrePreferences();

    /**
     * Find preferences with null ad rules
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.adRules IS NULL")
    List<TenantPreferences> findWithNullAdRules();

    /**
     * Find preferences updated in the last N days
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.updatedAt >= :daysAgo")
    List<TenantPreferences> findUpdatedInLastDays(@Param("daysAgo") java.time.LocalDateTime daysAgo);

    /**
     * Find preferences by tenant ID updated in the last N days
     */
    @Query("SELECT p FROM TenantPreferences p WHERE p.tenant.id = :tenantId AND p.updatedAt >= :daysAgo")
    List<TenantPreferences> findByTenantIdUpdatedInLastDays(@Param("tenantId") String tenantId, @Param("daysAgo") java.time.LocalDateTime daysAgo);

    /**
     * Count preferences by volume default
     */
    long countByVolumeDefault(Integer volumeDefault);

    /**
     * Count preferences by volume default range
     */
    long countByVolumeDefaultBetween(Integer minVolume, Integer maxVolume);
} 