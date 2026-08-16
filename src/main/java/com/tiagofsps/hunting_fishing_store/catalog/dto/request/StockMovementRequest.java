package com.tiagofsps.hunting_fishing_store.catalog.dto.request;

import com.tiagofsps.hunting_fishing_store.catalog.entity.MovementType;
import jakarta.validation.constraints.NotNull;

public record StockMovementRequest(
        @NotNull
        Long productItemId,

        @NotNull
        Integer quantity,

        @NotNull
        MovementType type,

        String notes,

        Long userId
) {
}
