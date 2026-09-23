package com.tiagofsps.hunting_fishing_store.payment.gateway;

import com.tiagofsps.hunting_fishing_store.common.exception.InvalidWebhookPayloadException;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidWebhookSignatureException;
import com.tiagofsps.hunting_fishing_store.config.PaymentProperties;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StubPaymentGatewayTest {

    private final PaymentProperties paymentProperties = mock(PaymentProperties.class);
    private final StubPaymentGateway gateway =
            new StubPaymentGateway(new ObjectMapper(), paymentProperties, "shared-secret");

    private Payment payment(PaymentMethod method, Order order) {
        Payment payment = new Payment();
        payment.setPaymentMethod(method);
        payment.setOrder(order);
        return payment;
    }

    @Test
    void shouldAcceptValidSignatureAndParseSuccess() {
        WebhookResult result = gateway.handleWebhook(
                "{\"paymentId\": 42, \"outcome\": \"SUCCESS\"}",
                "shared-secret");

        assertEquals(42L, result.paymentId());
        assertEquals(WebhookOutcome.SUCCESS, result.outcome());
    }

    @Test
    void shouldParseFailureOutcome() {
        WebhookResult result = gateway.handleWebhook(
                "{\"paymentId\": 7, \"outcome\": \"FAILURE\"}",
                "shared-secret");

        assertEquals(WebhookOutcome.FAILURE, result.outcome());
    }

    @Test
    void shouldRejectWrongSignature() {
        assertThrows(InvalidWebhookSignatureException.class, () ->
                gateway.handleWebhook("{\"paymentId\": 1, \"outcome\": \"SUCCESS\"}", "wrong-secret"));
    }

    @Test
    void shouldRejectMissingSignature() {
        assertThrows(InvalidWebhookSignatureException.class, () ->
                gateway.handleWebhook("{\"paymentId\": 1, \"outcome\": \"SUCCESS\"}", null));
    }

    @Test
    void shouldRejectMalformedPayloadWithValidSignature() {
        assertThrows(InvalidWebhookPayloadException.class, () ->
                gateway.handleWebhook("not-json", "shared-secret"));
    }

    @Test
    void shouldGenerateNineDigitMultibancoReferenceFromOrderId() {
        when(paymentProperties.getMultibancoEntity()).thenReturn("21345");
        when(paymentProperties.getMultibancoExpiryHours()).thenReturn(48L);
        when(paymentProperties.getCheckoutBaseUrl()).thenReturn("http://localhost:5173");

        Order order = new Order();
        order.setId(12L);
        GatewayResult result = gateway.initiate(payment(PaymentMethod.MULTIBANCO, order));

        assertNotNull(result.multibancoReference());
        assertEquals(9, result.multibancoReference().length());
        assertTrue(result.multibancoReference().startsWith("000012"));
        assertEquals("21345", result.multibancoEntity());
        assertNotNull(result.referenceExpiresAt());
    }

    @Test
    void shouldBuildPaymentUrlStubForCardPayments() {
        when(paymentProperties.getCheckoutBaseUrl()).thenReturn("http://localhost:5173");

        Order order = new Order();
        order.setOrderNumber("ORD-ABC");
        GatewayResult result = gateway.initiate(payment(PaymentMethod.CREDIT_CARD, order));

        assertEquals("http://localhost:5173/checkout/order/ORD-ABC", result.paymentUrl());
        assertNull(result.multibancoReference());
    }
}