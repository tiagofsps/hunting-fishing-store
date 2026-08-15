package com.tiagofsps.hunting_fishing_store.common.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(Long userId) {
        super("Cart for user with id '%d' was not found.".formatted(userId));
    }
}
