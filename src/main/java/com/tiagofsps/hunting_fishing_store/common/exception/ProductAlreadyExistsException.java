package com.tiagofsps.hunting_fishing_store.common.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String s) {
        super("Product with slug '%s' already exists.".formatted(s));
    }
}
