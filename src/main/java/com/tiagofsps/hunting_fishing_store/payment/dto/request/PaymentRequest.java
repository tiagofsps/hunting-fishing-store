package com.tiagofsps.hunting_fishing_store.payment.dto.request;

import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull
        Long orderId,

        @NotNull
        PaymentMethod paymentMethod,

        String mbwayPhone
) {
}