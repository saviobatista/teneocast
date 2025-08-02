package com.teneocast.tenant.repository;

import com.teneocast.tenant.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    /**
     * Find tenant by subdomain
     */
    Optional<Tenant> findBySubdomain(String subdomain);

    /**
     * Check if tenant exists by subdomain
     */
    boolean existsBySubdomain(String subdomain);

    /**
     * Find tenants by status
     */
    List<Tenant> findByStatus(Tenant.TenantStatus status);

    /**
     * Find tenants by status with pagination
     */
    Page<Tenant> findByStatus(Tenant.TenantStatus status, Pageable pageable);

    /**
     * Find tenants by name containing (case-insensitive)
     */
    List<Tenant> findByNameContainingIgnoreCase(String name);

    /**
     * Find tenants by name containing with pagination
     */
    Page<Tenant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find tenants by status and name containing
     */
    List<Tenant> findByStatusAndNameContainingIgnoreCase(Tenant.TenantStatus status, String name);

    /**
     * Find tenants by status and name containing with pagination
     */
    Page<Tenant> findByStatusAndNameContainingIgnoreCase(Tenant.TenantStatus status, String name, Pageable pageable);

    /**
     * Count tenants by status
     */
    long countByStatus(Tenant.TenantStatus status);

    /**
     * Find active tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE'")
    List<Tenant> findActiveTenants();

    /**
     * Find tenants created in the last N days
     */
    @Query("SELECT t FROM Tenant t WHERE t.createdAt >= :daysAgo")
    List<Tenant> findTenantsCreatedInLastDays(@Param("daysAgo") java.time.LocalDateTime daysAgo);

    /**
     * Find tenants with specific preferences
     */
    @Query("SELECT t FROM Tenant t WHERE t.preferences LIKE %:preference%")
    List<Tenant> findTenantsByPreference(@Param("preference") String preference);

    /**
     * Find tenants by subdomain pattern
     */
    @Query("SELECT t FROM Tenant t WHERE t.subdomain LIKE %:pattern%")
    List<Tenant> findTenantsBySubdomainPattern(@Param("pattern") String pattern);

    /**
     * Find tenants with null preferences
     */
    @Query("SELECT t FROM Tenant t WHERE t.preferences IS NULL")
    List<Tenant> findTenantsWithNullPreferences();

    /**
     * Find tenants updated in the last N days
     */
    @Query("SELECT t FROM Tenant t WHERE t.updatedAt >= :daysAgo")
    List<Tenant> findTenantsUpdatedInLastDays(@Param("daysAgo") java.time.LocalDateTime daysAgo);
} 