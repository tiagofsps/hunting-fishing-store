package com.tiagofsps.hunting_fishing_store.promotion.dto.response;

import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PromotionResponse(
        Long id,
        String name,
        DiscountType discountType,
        BigDecimal discountValue,
        Integer priority,
        Boolean stackable,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Boolean active,
        List<PromotionRuleResponse> rules
) {
}
