package com.tiagofsps.hunting_fishing_store.common.exception;

public class PromotionNotFoundException extends RuntimeException {
    public PromotionNotFoundException(Long id) {
        super("Promotion with id '%d' was not found.".formatted(id));
    }
}
