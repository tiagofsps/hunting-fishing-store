package com.tiagofsps.hunting_fishing_store.coupon.service;

import com.tiagofsps.hunting_fishing_store.common.exception.CouponInvalidException;
import com.tiagofsps.hunting_fishing_store.common.exception.CouponNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.CouponUsageLimitReachedException;
import com.tiagofsps.hunting_fishing_store.coupon.dto.request.CouponRequest;
import com.tiagofsps.hunting_fishing_store.coupon.dto.response.CouponResponse;
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.coupon.entity.CouponUsage;
import com.tiagofsps.hunting_fishing_store.coupon.repository.CouponRepository;
import com.tiagofsps.hunting_fishing_store.coupon.repository.CouponUsageRepository;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Should throw when coupon is expired")
    void shouldThrowWhenExpired() {
        Coupon coupon = new Coupon();
        coupon.setCode("OLD");
        coupon.setValidFrom(LocalDateTime.now().minusDays(10));
        coupon.setValidTo(LocalDateTime.now().minusDays(1));
        coupon.setActive(true);

        when(couponRepository.findByCode("OLD")).thenReturn(Optional.of(coupon));

        assertThrows(CouponInvalidException.class, () -> couponService.validateAndGet("OLD", 1L, BigDecimal.valueOf(100.0)));
    }

    @Test
    @DisplayName("Should throw when global usage limit reached")
    void shouldThrowWhenGlobalLimitReached() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("LIMIT");
        coupon.setUsageLimit(5);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);

        when(couponRepository.findByCode("LIMIT")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.countByCouponId(1L)).thenReturn(5L);

        assertThrows(CouponUsageLimitReachedException.class, () -> couponService.validateAndGet("LIMIT", 1L, BigDecimal.valueOf(100.0)));
    }

    @Test
    @DisplayName("Should validate and return coupon when all criteria met")
    void shouldValidateAndReturnCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("VALID");
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);

        when(couponRepository.findByCode("VALID")).thenReturn(Optional.of(coupon));

        Coupon result = couponService.validateAndGet("VALID", 1L, BigDecimal.valueOf(100.0));

        assertNotNull(result);
        assertEquals("VALID", result.getCode());
    }

    @Test
    @DisplayName("Should throw when coupon code not found")
    void shouldThrowWhenCodeNotFound() {
        when(couponRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> couponService.validateAndGet("UNKNOWN", 1L, BigDecimal.valueOf(100.0)));
    }

    @Test
    @DisplayName("Should throw when coupon is inactive")
    void shouldThrowWhenInactive() {
        Coupon coupon = new Coupon();
        coupon.setCode("INACTIVE");
        coupon.setActive(false);

        when(couponRepository.findByCode("INACTIVE")).thenReturn(Optional.of(coupon));

        assertThrows(CouponInvalidException.class, () -> couponService.validateAndGet("INACTIVE", 1L, BigDecimal.valueOf(100.0)));
    }

    @Test
    @DisplayName("Should throw when user usage limit reached")
    void shouldThrowWhenUserLimitReached() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("USERLIMIT");
        coupon.setUsageLimitPerUser(1);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);

        when(couponRepository.findByCode("USERLIMIT")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 1L)).thenReturn(1L);

        assertThrows(CouponInvalidException.class, () -> couponService.validateAndGet("USERLIMIT", 1L, BigDecimal.valueOf(100.0)));
    }

    @Test
    @DisplayName("Should calculate fixed amount discount")
    void shouldCalculateFixedAmountDiscount() {
        Coupon coupon = new Coupon();
        coupon.setDiscountType(DiscountType.FIXED_AMOUNT);
        coupon.setDiscountValue(BigDecimal.valueOf(15.0));

        BigDecimal discount = couponService.calculateDiscount(coupon, BigDecimal.valueOf(100.0));

        assertEquals(0, discount.compareTo(BigDecimal.valueOf(15.0)));
    }

    @Test
    @DisplayName("Should cap fixed discount at order total")
    void shouldCapFixedDiscountAtTotal() {
        Coupon coupon = new Coupon();
        coupon.setDiscountType(DiscountType.FIXED_AMOUNT);
        coupon.setDiscountValue(BigDecimal.valueOf(50.0));

        BigDecimal discount = couponService.calculateDiscount(coupon, BigDecimal.valueOf(30.0));

        assertEquals(0, discount.compareTo(BigDecimal.valueOf(30.0)));
    }

    @Test
    @DisplayName("Should create a new coupon")
    void shouldCreateCoupon() {
        CouponRequest request = new CouponRequest("NEWCODE", DiscountType.PERCENTAGE, BigDecimal.valueOf(10), null, null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> {
            Coupon c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        CouponResponse response = couponService.create(request);

        assertEquals("NEWCODE", response.code());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Should return all coupons")
    void shouldReturnAllCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("CODE1");
        when(couponRepository.findAll()).thenReturn(java.util.List.of(coupon));

        var result = couponService.findAll();

        assertEquals(1, result.size());
        assertEquals("CODE1", result.get(0).code());
    }

    @Test
    @DisplayName("Should find coupon by id")
    void shouldFindById() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("IDCODE");
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        CouponResponse response = couponService.findById(1L);

        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("Should record coupon usage")
    void shouldRecordUsage() {
        Coupon coupon = new Coupon();
        User user = new User();
        Order order = new Order();

        couponService.recordUsage(coupon, user, order);

        verify(couponUsageRepository).save(any(CouponUsage.class));
    }
}
