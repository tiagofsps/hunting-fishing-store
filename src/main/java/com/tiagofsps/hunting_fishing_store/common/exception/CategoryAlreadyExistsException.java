package com.tiagofsps.hunting_fishing_store.common.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String s) {
        super("Category with slug '%s' already exists.".formatted(s));
    }
}
