package com.tiagofsps.hunting_fishing_store.promotion.dto.request;

import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionTargetType;
import jakarta.validation.constraints.NotNull;

public record PromotionRuleRequest(
        @NotNull
        PromotionTargetType targetType,

        @NotNull
        Long targetId
) {
}
