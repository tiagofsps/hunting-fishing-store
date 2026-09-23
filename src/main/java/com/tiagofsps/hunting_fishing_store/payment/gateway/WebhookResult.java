package com.tiagofsps.hunting_fishing_store.payment.gateway;

public record WebhookResult(
        Long paymentId,
        WebhookOutcome outcome
) {
}