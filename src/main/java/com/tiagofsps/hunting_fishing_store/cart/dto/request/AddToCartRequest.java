package com.tiagofsps.hunting_fishing_store.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddToCartRequest(
        @NotNull
        Long productItemId,

        @NotNull
        @Positive
        Integer quantity
) {
}
