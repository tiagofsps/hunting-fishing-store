package com.tiagofsps.hunting_fishing_store.common.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User with id '%d' was not found.".formatted(id));
    }

    public UserNotFoundException(String email) {
        super("User with email '%s' was not found.".formatted(email));
    }
}
