package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

import com.tiagofsps.hunting_fishing_store.catalog.entity.MovementType;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long productItemId,
        String sku,
        Integer quantity,
        MovementType type,
        String notes,
        Long userId,
        LocalDateTime createdAt
) {
}
