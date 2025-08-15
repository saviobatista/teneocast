package com.teneocast.admin.repository;

import com.teneocast.admin.entity.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, UUID> {

    Optional<PlatformSettings> findBySettingKey(String settingKey);

    List<PlatformSettings> findBySettingType(PlatformSettings.SettingType settingType);

    @Query("SELECT ps FROM PlatformSettings ps WHERE ps.settingKey LIKE %:pattern%")
    List<PlatformSettings> findBySettingKeyPattern(@Param("pattern") String pattern);

    @Query("SELECT ps FROM PlatformSettings ps WHERE ps.updatedBy.id = :adminUserId")
    List<PlatformSettings> findByUpdatedBy(@Param("adminUserId") UUID adminUserId);

    boolean existsBySettingKey(String settingKey);

    @Query("SELECT COUNT(ps) FROM PlatformSettings ps")
    long countSettings();

    @Query("SELECT ps FROM PlatformSettings ps ORDER BY ps.updatedAt DESC")
    List<PlatformSettings> findRecentlyUpdatedSettings();
}
