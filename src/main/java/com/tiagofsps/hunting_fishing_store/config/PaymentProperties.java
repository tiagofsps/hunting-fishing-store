package com.tiagofsps.hunting_fishing_store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * PSP/gateway configuration for payments. Values come from application.yml
 * (app.payment.*) and fall back to the defaults below when absent.
 */
@Component
public class PaymentProperties {

    @Value("${app.payment.multibanco.entity:21345}")
    private String multibancoEntity = "21345";

    @Value("${app.payment.multibanco.expiry-hours:48}")
    private long multibancoExpiryHours = 48;

    @Value("${app.payment.checkout-base-url:http://localhost:5173}")
    private String checkoutBaseUrl = "http://localhost:5173";

    public String getMultibancoEntity() {
        return multibancoEntity;
    }

    public long getMultibancoExpiryHours() {
        return multibancoExpiryHours;
    }

    public String getCheckoutBaseUrl() {
        return checkoutBaseUrl;
    }
}