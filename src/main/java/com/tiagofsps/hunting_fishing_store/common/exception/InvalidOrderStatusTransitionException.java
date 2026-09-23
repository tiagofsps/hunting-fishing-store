package com.tiagofsps.hunting_fishing_store.common.exception;

import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Invalid order status transition from %s to %s".formatted(from, to));
    }

    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}
