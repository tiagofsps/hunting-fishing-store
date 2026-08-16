package com.tiagofsps.hunting_fishing_store.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productItemId,
        String productName,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
) {
}
