package com.tiagofsps.hunting_fishing_store.common.exception;

public class InvalidWebhookSignatureException extends RuntimeException {

    public InvalidWebhookSignatureException() {
        super("Invalid payment webhook signature");
    }
}