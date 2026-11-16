package com.tiagofsps.hunting_fishing_store.common.exception;

public class NewsletterSubscriptionNotFoundException extends RuntimeException {

    public NewsletterSubscriptionNotFoundException(Long id) {
        super("Newsletter subscription with id '%d' was not found.".formatted(id));
    }
}
