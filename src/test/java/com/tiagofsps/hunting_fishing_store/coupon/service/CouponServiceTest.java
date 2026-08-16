package com.tiagofsps.hunting_fishing_store.coupon.service;

import com.tiagofsps.hunting_fishing_store.common.exception.CouponInvalidException;
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.coupon.repository.CouponRepository;
import com.tiagofsps.hunting_fishing_store.coupon.repository.CouponUsageRepository;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("Should calculate percentage discount")
    void shouldCalculatePercentageDiscount() {
        Coupon coupon = new Coupon();
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(10.0));

        BigDecimal discount = couponService.calculateDiscount(coupon, BigDecimal.valueOf(100.0));

        assertEquals(0, discount.compareTo(BigDecimal.valueOf(10.0)));
    }

    @Test
    @DisplayName("Should throw exception when order total is below minimum")
    void shouldThrowWhenBelowMinimum() {
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE10");
        coupon.setMinOrderAmount(BigDecimal.valueOf(50.0));
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);

        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        assertThrows(CouponInvalidException.class, () -> couponService.validateAndGet("SAVE10", 1L, BigDecimal.valueOf(30.0)));
    }
}
