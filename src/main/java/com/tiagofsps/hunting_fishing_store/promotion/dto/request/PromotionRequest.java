package com.tiagofsps.hunting_fishing_store.promotion.dto.request;

import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PromotionRequest(
        @NotBlank
        String name,

        @NotNull
        DiscountType discountType,

        @NotNull
        @Positive
        BigDecimal discountValue,

        Integer priority,

        Boolean stackable,

        @NotNull
        LocalDateTime validFrom,

        @NotNull
        LocalDateTime validTo,

        List<PromotionRuleRequest> rules
) {
}
