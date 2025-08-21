package com.teneocast.admin.repository;

import com.teneocast.admin.entity.AdminUser;
import com.teneocast.common.dto.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

    Optional<AdminUser> findByEmail(String email);

    Optional<AdminUser> findByEmailAndIsActiveTrue(String email);

    List<AdminUser> findByRole(UserRole role);

    List<AdminUser> findByIsActiveTrue();

    @Query("SELECT a FROM AdminUser a WHERE a.isActive = true AND a.role IN (:roles)")
    List<AdminUser> findByActiveRoles(@Param("roles") List<UserRole> roles);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(a) FROM AdminUser a WHERE a.isActive = true")
    long countActiveUsers();

    @Query("SELECT a FROM AdminUser a WHERE a.lastLoginAt IS NOT NULL ORDER BY a.lastLoginAt DESC")
    List<AdminUser> findRecentlyActiveUsers();
}
