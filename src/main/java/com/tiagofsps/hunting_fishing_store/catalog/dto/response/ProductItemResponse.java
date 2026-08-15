package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record ProductItemResponse(
        Long id,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Double weight,
        Boolean active,
        Map<String, Object> attributes
) {
}
