package com.tiagofsps.hunting_fishing_store.common.exception;

public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(Long id) {
        super("Brand with id '%d' was not found.".formatted(id));
    }
}