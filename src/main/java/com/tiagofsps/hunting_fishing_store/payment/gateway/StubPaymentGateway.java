package com.tiagofsps.hunting_fishing_store.payment.gateway;

import com.tiagofsps.hunting_fishing_store.common.exception.InvalidWebhookPayloadException;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidWebhookSignatureException;
import com.tiagofsps.hunting_fishing_store.config.PaymentProperties;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Default gateway stub. No real PSP is integrated yet: initiation returns
 * placeholder data and the webhook path parses a minimal
 * {"paymentId": <id>, "outcome": "SUCCESS"|"FAILURE"} payload signed by a
 * shared secret (constant-time compare). Disable by setting
 * app.payment.gateway to anything but "stub" when a real provider lands.
 */
@Service
@ConditionalOnProperty(name = "app.payment.gateway", havingValue = "stub", matchIfMissing = true)
public class StubPaymentGateway implements PaymentGateway {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ObjectMapper objectMapper;
    private final PaymentProperties paymentProperties;
    private final String webhookSecret;

    public StubPaymentGateway(
            ObjectMapper objectMapper,
            PaymentProperties paymentProperties,
            @Value("${app.payment.webhook-secret:local-webhook-secret}") String webhookSecret) {
        this.objectMapper = objectMapper;
        this.paymentProperties = paymentProperties;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public GatewayResult initiate(Payment payment) {
        Order order = payment.getOrder();
        return switch (payment.getPaymentMethod()) {
            case MULTIBANCO -> multibancoResult(order);
            case CREDIT_CARD, KLARNA -> urlResult(order);
            case MBWAY -> new GatewayResult(null, null, null, null);
        };
    }

    @Override
    public WebhookResult handleWebhook(String payload, String signatureHeader) {
        if (!isValidSignature(signatureHeader)) {
            throw new InvalidWebhookSignatureException();
        }

        try {
            Map<String, Object> body = objectMapper.readValue(payload, Map.class);
            Long paymentId = Long.valueOf(String.valueOf(body.get("paymentId")));
            WebhookOutcome outcome = WebhookOutcome.from(String.valueOf(body.get("outcome")));
            return new WebhookResult(paymentId, outcome);
        } catch (Exception ex) {
            throw new InvalidWebhookPayloadException("Malformed webhook payload: " + ex.getMessage());
        }
    }

    private boolean isValidSignature(String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank() || signatureHeader == null) {
            return false;
        }
        return MessageDigest.isEqual(
                webhookSecret.getBytes(StandardCharsets.UTF_8),
                signatureHeader.getBytes(StandardCharsets.UTF_8));
    }

    private GatewayResult multibancoResult(Order order) {
        return new GatewayResult(
                null,
                paymentProperties.getMultibancoEntity(),
                generateMultibancoReference(order),
                LocalDateTime.now().plusHours(paymentProperties.getMultibancoExpiryHours()));
    }

    private GatewayResult urlResult(Order order) {
        // Stub: points at the future checkout page until a PSP is integrated.
        String url = paymentProperties.getCheckoutBaseUrl() + "/checkout/order/" + order.getOrderNumber();
        return new GatewayResult(url, null, null, null);
    }

    String generateMultibancoReference(Order order) {
        String deterministic = String.format("%06d", Math.floorMod(order.getId(), 1_000_000L));
        String random = String.format("%03d", RANDOM.nextInt(1000));
        return deterministic + random;
    }
}