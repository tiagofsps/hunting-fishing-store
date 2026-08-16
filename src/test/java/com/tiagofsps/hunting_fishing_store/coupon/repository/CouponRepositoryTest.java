package com.tiagofsps.hunting_fishing_store.coupon.repository;

import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest
class CouponRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

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
