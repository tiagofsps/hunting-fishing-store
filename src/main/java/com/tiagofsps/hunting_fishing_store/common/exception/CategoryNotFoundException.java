package com.tiagofsps.hunting_fishing_store.common.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("Category with id '%d' was not found.".formatted(id));
    }
}
