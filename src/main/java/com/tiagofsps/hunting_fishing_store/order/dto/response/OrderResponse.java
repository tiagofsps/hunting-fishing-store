package com.tiagofsps.hunting_fishing_store.order.dto.response;

import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal totalAmount,
        BigDecimal shippingCost,
        Long userId,
        List<OrderItemResponse> items
) {
}
