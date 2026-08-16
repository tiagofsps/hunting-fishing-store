package com.tiagofsps.hunting_fishing_store.coupon.dto.response;

import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        Integer usageLimit,
        Integer usageLimitPerUser,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Boolean active
) {
}
