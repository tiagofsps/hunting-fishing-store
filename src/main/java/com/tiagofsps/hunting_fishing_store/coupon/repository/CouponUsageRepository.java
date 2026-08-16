package com.tiagofsps.hunting_fishing_store.coupon.repository;

import com.tiagofsps.hunting_fishing_store.coupon.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    long countByCouponId(Long couponId);
    long countByCouponIdAndUserId(Long couponId, Long userId);
}
