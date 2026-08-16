package com.tiagofsps.hunting_fishing_store.promotion.dto.response;

import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionTargetType;

public record PromotionRuleResponse(
        Long id,
        PromotionTargetType targetType,
        Long targetId
) {
}
