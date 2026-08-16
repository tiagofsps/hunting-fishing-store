package com.tiagofsps.hunting_fishing_store.coupon.dto.request;

import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank
        String code,

        @NotNull
        DiscountType discountType,

        @NotNull
        @Positive
        BigDecimal discountValue,

        @Positive
        BigDecimal minOrderAmount,

        Integer usageLimit,

        Integer usageLimitPerUser,

        @NotNull
        LocalDateTime validFrom,

        @NotNull
        LocalDateTime validTo
) {
}
