package com.tiagofsps.hunting_fishing_store.common.exception;

public class InvalidWebhookPayloadException extends RuntimeException {

    public InvalidWebhookPayloadException(String message) {
        super(message);
    }
}