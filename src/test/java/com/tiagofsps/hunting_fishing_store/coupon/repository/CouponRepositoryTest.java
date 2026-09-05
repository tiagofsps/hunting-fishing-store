package com.tiagofsps.hunting_fishing_store.coupon.repository;

import com.tiagofsps.hunting_fishing_store.BaseRepositoryTest;
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldFindByCode() {
        Coupon coupon = new Coupon();
        coupon.setCode("WELCOME");
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(5));
        coupon.setValidFrom(LocalDateTime.now());
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        couponRepository.save(coupon);

        Optional<Coupon> result = couponRepository.findByCode("WELCOME");

        assertTrue(result.isPresent());
    }
}
