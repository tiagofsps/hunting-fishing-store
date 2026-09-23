package com.tiagofsps.hunting_fishing_store.common.exception;

public class NewsletterAlreadySubscribedException extends RuntimeException {

    public NewsletterAlreadySubscribedException(String email) {
        super("Email is already subscribed to the newsletter: " + email);
    }
}