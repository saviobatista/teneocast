package com.teneocast.tenant.repository;

import com.teneocast.tenant.entity.TenantSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, String> {

    /**
     * Find subscription by tenant ID
     */
    Optional<TenantSubscription> findByTenantId(String tenantId);

    /**
     * Check if subscription exists by tenant ID
     */
    boolean existsByTenantId(String tenantId);

    /**
     * Find subscriptions by plan type
     */
    List<TenantSubscription> findByPlanType(TenantSubscription.PlanType planType);

    /**
     * Find subscriptions by plan type with pagination
     */
    Page<TenantSubscription> findByPlanType(TenantSubscription.PlanType planType, Pageable pageable);

    /**
     * Find subscriptions by billing cycle
     */
    List<TenantSubscription> findByBillingCycle(TenantSubscription.BillingCycle billingCycle);

    /**
     * Find subscriptions by billing cycle with pagination
     */
    Page<TenantSubscription> findByBillingCycle(TenantSubscription.BillingCycle billingCycle, Pageable pageable);

    /**
     * Find active subscriptions
     */
    List<TenantSubscription> findByIsActiveTrue();

    /**
     * Find active subscriptions with pagination
     */
    Page<TenantSubscription> findByIsActiveTrue(Pageable pageable);

    /**
     * Find inactive subscriptions
     */
    List<TenantSubscription> findByIsActiveFalse();

    /**
     * Find inactive subscriptions with pagination
     */
    Page<TenantSubscription> findByIsActiveFalse(Pageable pageable);

    /**
     * Find subscriptions by plan type and active status
     */
    List<TenantSubscription> findByPlanTypeAndIsActive(TenantSubscription.PlanType planType, Boolean isActive);

    /**
     * Find subscriptions by plan type and active status with pagination
     */
    Page<TenantSubscription> findByPlanTypeAndIsActive(TenantSubscription.PlanType planType, Boolean isActive, Pageable pageable);

    /**
     * Find subscriptions by max users
     */
    List<TenantSubscription> findByMaxUsers(Integer maxUsers);

    /**
     * Find subscriptions by max users range
     */
    List<TenantSubscription> findByMaxUsersBetween(Integer minUsers, Integer maxUsers);

    /**
     * Find subscriptions by max storage
     */
    List<TenantSubscription> findByMaxStorageGb(Integer maxStorageGb);

    /**
     * Find subscriptions by max storage range
     */
    List<TenantSubscription> findByMaxStorageGbBetween(Integer minStorage, Integer maxStorage);

    /**
     * Find subscriptions expiring soon
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.nextBillingDate <= :expiryDate AND s.isActive = true")
    List<TenantSubscription> findSubscriptionsExpiringBy(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find subscriptions by tenant ID expiring soon
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.tenant.id = :tenantId AND s.nextBillingDate <= :expiryDate AND s.isActive = true")
    List<TenantSubscription> findSubscriptionsByTenantIdExpiringBy(@Param("tenantId") String tenantId, @Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find subscriptions with null next billing date
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.nextBillingDate IS NULL")
    List<TenantSubscription> findSubscriptionsWithNullNextBillingDate();

    /**
     * Find subscriptions by tenant ID with null next billing date
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.tenant.id = :tenantId AND s.nextBillingDate IS NULL")
    List<TenantSubscription> findSubscriptionsByTenantIdWithNullNextBillingDate(@Param("tenantId") String tenantId);

    /**
     * Find subscriptions by plan name
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.planName LIKE %:planName%")
    List<TenantSubscription> findByPlanNameContaining(@Param("planName") String planName);

    /**
     * Find subscriptions by tenant ID and plan name
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.tenant.id = :tenantId AND s.planName LIKE %:planName%")
    List<TenantSubscription> findByTenantIdAndPlanNameContaining(@Param("tenantId") String tenantId, @Param("planName") String planName);

    /**
     * Find subscriptions updated in the last N days
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.updatedAt >= :daysAgo")
    List<TenantSubscription> findUpdatedInLastDays(@Param("daysAgo") LocalDateTime daysAgo);

    /**
     * Find subscriptions by tenant ID updated in the last N days
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.tenant.id = :tenantId AND s.updatedAt >= :daysAgo")
    List<TenantSubscription> findByTenantIdUpdatedInLastDays(@Param("tenantId") String tenantId, @Param("daysAgo") LocalDateTime daysAgo);

    /**
     * Count subscriptions by plan type
     */
    long countByPlanType(TenantSubscription.PlanType planType);

    /**
     * Count subscriptions by billing cycle
     */
    long countByBillingCycle(TenantSubscription.BillingCycle billingCycle);

    /**
     * Count active subscriptions
     */
    long countByIsActiveTrue();

    /**
     * Count inactive subscriptions
     */
    long countByIsActiveFalse();

    /**
     * Count subscriptions by plan type and active status
     */
    long countByPlanTypeAndIsActive(TenantSubscription.PlanType planType, Boolean isActive);

    /**
     * Count subscriptions by max users
     */
    long countByMaxUsers(Integer maxUsers);

    /**
     * Count subscriptions by max storage
     */
    long countByMaxStorageGb(Integer maxStorageGb);
} 