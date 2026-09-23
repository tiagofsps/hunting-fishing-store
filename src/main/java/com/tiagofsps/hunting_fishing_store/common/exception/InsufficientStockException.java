package com.tiagofsps.hunting_fishing_store.common.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, Integer requested, Integer available) {
        super("Insufficient stock for SKU '%s'. Requested: %d, Available: %d".formatted(sku, requested, available));
    }

    public InsufficientStockException(String productName, String sku, Integer requested, Integer available) {
        super("Insufficient stock for product '%s' (SKU '%s'). Requested: %d, Available: %d"
                .formatted(productName, sku, requested, available));
    }
}
