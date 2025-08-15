package com.teneocast.media.repository;

import com.teneocast.media.entity.AdType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdTypeRepository extends JpaRepository<AdType, Long> {
    
    Optional<AdType> findByName(String name);
    
    @Query("SELECT at FROM AdType at WHERE at.tenantId = :tenantId OR at.tenantId IS NULL")
    List<AdType> findByTenantIdOrGlobal(@Param("tenantId") UUID tenantId);
    
    @Query("SELECT at FROM AdType at WHERE at.tenantId = :tenantId")
    List<AdType> findByTenantId(@Param("tenantId") UUID tenantId);
    
    boolean existsByNameAndTenantId(String name, UUID tenantId);
}
