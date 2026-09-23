package com.tiagofsps.hunting_fishing_store.payment.gateway;

import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;

/**
 * PSP (payment service provider) integration seam. The only production
 * implementation is {@link StubPaymentGateway}; a real provider can be dropped
 * in later by implementing this interface and toggling app.payment.gateway.
 */
public interface PaymentGateway {

    String SIGNATURE_HEADER = "X-Webhook-Signature";

    /**
     * Produces the PSP-specific bits of a payment initiation (redirect URL,
     * Multibanco entity/reference/expiry). The service layer persists the
     * result and enforces business rules (ownership, PENDING-only, amount from
     * order, reference uniqueness).
     */
    GatewayResult initiate(Payment payment);

    /**
     * Verifies the webhook signature and parses the provider payload into a
     * normalized outcome. Throws InvalidWebhookSignatureException for a bad or
     * missing signature and InvalidWebhookPayloadException for an unparseable
     * body.
     */
    WebhookResult handleWebhook(String payload, String signatureHeader);
}