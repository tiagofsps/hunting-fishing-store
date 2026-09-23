package com.tiagofsps.hunting_fishing_store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Shipping cost rules. Values come from application.yml (app.shipping.*) and
 * fall back to the defaults below when the properties are absent.
 */
@Component
public class ShippingProperties {

    @Value("${app.shipping.free-threshold:75}")
    private BigDecimal freeThreshold = new BigDecimal("75");

    @Value("${app.shipping.fee:4.95}")
    private BigDecimal fee = new BigDecimal("4.95");

    public BigDecimal getFreeThreshold() {
        return freeThreshold;
    }

    public BigDecimal getFee() {
        return fee;
    }
}
