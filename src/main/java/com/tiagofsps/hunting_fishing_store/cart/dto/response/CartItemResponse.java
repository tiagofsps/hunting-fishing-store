package com.tiagofsps.hunting_fishing_store.cart.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productItemId,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subTotal
) {
}
