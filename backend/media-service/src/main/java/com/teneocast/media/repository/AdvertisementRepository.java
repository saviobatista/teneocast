package com.teneocast.media.repository;

import com.teneocast.media.entity.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    
    @Query("SELECT a FROM Advertisement a WHERE a.tenantId = :tenantId")
    Page<Advertisement> findByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);
    
    @Query("SELECT a FROM Advertisement a WHERE a.tenantId = :tenantId AND a.adType.id = :adTypeId")
    Page<Advertisement> findByTenantIdAndAdTypeId(@Param("tenantId") UUID tenantId, @Param("adTypeId") Long adTypeId, Pageable pageable);
    
    @Query("SELECT a FROM Advertisement a WHERE a.tenantId = :tenantId AND LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Advertisement> findByTenantIdAndSearchTerm(@Param("tenantId") UUID tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT a FROM Advertisement a WHERE a.tenantId = :tenantId")
    List<Advertisement> findAllByTenantId(@Param("tenantId") UUID tenantId);
    
    boolean existsByTenantIdAndName(UUID tenantId, String name);
}
