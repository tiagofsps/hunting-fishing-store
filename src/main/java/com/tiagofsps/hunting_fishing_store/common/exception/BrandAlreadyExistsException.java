package com.tiagofsps.hunting_fishing_store.common.exception;

public class BrandAlreadyExistsException extends RuntimeException {

    public BrandAlreadyExistsException(String s) {
        super("Brand with slug '%s' already exists.".formatted(s));
    }
}
