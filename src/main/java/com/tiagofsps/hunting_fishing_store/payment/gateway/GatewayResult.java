package com.tiagofsps.hunting_fishing_store.payment.gateway;

import java.time.LocalDateTime;

/**
 * PSP-specific payment initiation data. Fields not applicable to the chosen
 * method are null.
 */
public record GatewayResult(
        String paymentUrl,
        String multibancoEntity,
        String multibancoReference,
        LocalDateTime referenceExpiresAt
) {
}