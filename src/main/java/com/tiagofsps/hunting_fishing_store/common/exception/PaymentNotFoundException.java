package com.tiagofsps.hunting_fishing_store.common.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("Payment with id '%d' was not found.".formatted(id));
    }

    public PaymentNotFoundException(String transactionId) {
        super("Payment with transaction id '%s' was not found.".formatted(transactionId));
    }
}
