package com.teneocast.tenant.repository;

import com.teneocast.tenant.entity.TenantUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, String> {

    /**
     * Find user by tenant ID and email
     */
    Optional<TenantUser> findByTenantIdAndEmail(String tenantId, String email);

    /**
     * Find users by tenant ID
     */
    List<TenantUser> findByTenantId(String tenantId);

    /**
     * Find users by tenant ID with pagination
     */
    Page<TenantUser> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find users by tenant ID and role
     */
    List<TenantUser> findByTenantIdAndRole(String tenantId, TenantUser.UserRole role);

    /**
     * Find users by tenant ID and role with pagination
     */
    Page<TenantUser> findByTenantIdAndRole(String tenantId, TenantUser.UserRole role, Pageable pageable);

    /**
     * Find users by tenant ID and active status
     */
    List<TenantUser> findByTenantIdAndIsActive(String tenantId, Boolean isActive);

    /**
     * Find users by tenant ID and active status with pagination
     */
    Page<TenantUser> findByTenantIdAndIsActive(String tenantId, Boolean isActive, Pageable pageable);

    /**
     * Find users by email (across all tenants)
     */
    List<TenantUser> findByEmail(String email);

    /**
     * Find users by role (across all tenants)
     */
    List<TenantUser> findByRole(TenantUser.UserRole role);

    /**
     * Find active users by tenant ID
     */
    @Query("SELECT u FROM TenantUser u WHERE u.tenant.id = :tenantId AND u.isActive = true")
    List<TenantUser> findActiveUsersByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find users by tenant ID and email pattern
     */
    @Query("SELECT u FROM TenantUser u WHERE u.tenant.id = :tenantId AND u.email LIKE %:emailPattern%")
    List<TenantUser> findUsersByTenantIdAndEmailPattern(@Param("tenantId") String tenantId, @Param("emailPattern") String emailPattern);

    /**
     * Find users by tenant ID and role with email containing
     */
    @Query("SELECT u FROM TenantUser u WHERE u.tenant.id = :tenantId AND u.role = :role AND u.email LIKE %:emailPattern%")
    List<TenantUser> findUsersByTenantIdAndRoleAndEmailPattern(@Param("tenantId") String tenantId, @Param("role") TenantUser.UserRole role, @Param("emailPattern") String emailPattern);

    /**
     * Count users by tenant ID
     */
    long countByTenantId(String tenantId);

    /**
     * Count users by tenant ID and role
     */
    long countByTenantIdAndRole(String tenantId, TenantUser.UserRole role);

    /**
     * Count active users by tenant ID
     */
    long countByTenantIdAndIsActive(String tenantId, Boolean isActive);

    /**
     * Find users who logged in recently
     */
    @Query("SELECT u FROM TenantUser u WHERE u.lastLoginAt >= :since")
    List<TenantUser> findUsersLoggedInSince(@Param("since") java.time.LocalDateTime since);

    /**
     * Find users by tenant ID who logged in recently
     */
    @Query("SELECT u FROM TenantUser u WHERE u.tenant.id = :tenantId AND u.lastLoginAt >= :since")
    List<TenantUser> findUsersByTenantIdLoggedInSince(@Param("tenantId") String tenantId, @Param("since") java.time.LocalDateTime since);

    /**
     * Find users with null last login
     */
    @Query("SELECT u FROM TenantUser u WHERE u.lastLoginAt IS NULL")
    List<TenantUser> findUsersWithNullLastLogin();

    /**
     * Find users by tenant ID with null last login
     */
    @Query("SELECT u FROM TenantUser u WHERE u.tenant.id = :tenantId AND u.lastLoginAt IS NULL")
    List<TenantUser> findUsersByTenantIdWithNullLastLogin(@Param("tenantId") String tenantId);
} 