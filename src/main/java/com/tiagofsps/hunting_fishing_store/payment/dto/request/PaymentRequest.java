package com.tiagofsps.hunting_fishing_store.payment.dto.request;

import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull
        Long orderId,

        @NotNull
        PaymentMethod paymentMethod,

        @NotNull
        @Positive
        BigDecimal amount
) {
}
