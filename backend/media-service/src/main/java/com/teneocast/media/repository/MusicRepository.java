package com.teneocast.media.repository;

import com.teneocast.media.entity.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    
    @Query("SELECT m FROM Music m WHERE m.tenantId = :tenantId")
    Page<Music> findByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);
    
    @Query("SELECT m FROM Music m WHERE m.tenantId = :tenantId AND m.genre.id = :genreId")
    Page<Music> findByTenantIdAndGenreId(@Param("tenantId") UUID tenantId, @Param("genreId") Long genreId, Pageable pageable);
    
    @Query("SELECT m FROM Music m WHERE m.tenantId = :tenantId AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(m.artist) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Music> findByTenantIdAndSearchTerm(@Param("tenantId") UUID tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT m FROM Music m WHERE m.tenantId = :tenantId")
    List<Music> findAllByTenantId(@Param("tenantId") UUID tenantId);
    
    boolean existsByTenantIdAndTitleAndArtist(UUID tenantId, String title, String artist);
}
