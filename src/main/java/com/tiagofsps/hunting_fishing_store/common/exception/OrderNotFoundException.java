package com.tiagofsps.hunting_fishing_store.common.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order with id '%d' was not found.".formatted(id));
    }

    public OrderNotFoundException(String orderNumber) {
        super("Order with number '%s' was not found.".formatted(orderNumber));
    }
}
