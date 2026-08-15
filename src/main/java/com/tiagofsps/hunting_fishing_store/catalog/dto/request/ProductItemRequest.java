package com.tiagofsps.hunting_fishing_store.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Map;

public record ProductItemRequest(
        @NotBlank
        String sku,

        @NotNull
        @PositiveOrZero
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer stockQuantity,

        Double weight,

        Boolean active,

        Map<String, Object> attributes
) {
}
