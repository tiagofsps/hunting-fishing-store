package com.tiagofsps.hunting_fishing_store.payment.gateway;

public enum WebhookOutcome {
    SUCCESS,
    FAILURE;

    public static WebhookOutcome from(String value) {
        if ("SUCCESS".equalsIgnoreCase(value) || "PAID".equalsIgnoreCase(value)) {
            return SUCCESS;
        }
        if ("FAILURE".equalsIgnoreCase(value) || "FAILED".equalsIgnoreCase(value)) {
            return FAILURE;
        }
        throw new IllegalArgumentException("Unknown webhook outcome: " + value);
    }
}