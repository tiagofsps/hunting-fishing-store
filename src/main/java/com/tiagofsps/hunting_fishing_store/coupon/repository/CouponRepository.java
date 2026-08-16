package com.tiagofsps.hunting_fishing_store.coupon.repository;

import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
}
