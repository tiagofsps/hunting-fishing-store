package com.tiagofsps.hunting_fishing_store.common.exception;

public class InvalidPaymentRequestException extends RuntimeException {

    public InvalidPaymentRequestException(String message) {
        super(message);
    }
}