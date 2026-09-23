package com.tiagofsps.hunting_fishing_store.order.dto.request;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull
        Long shippingAddressId,

        Long billingAddressId,

        String couponCode
) {
}
