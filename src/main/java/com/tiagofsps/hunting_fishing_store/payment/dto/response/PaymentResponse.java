package com.tiagofsps.hunting_fishing_store.payment.dto.response;

import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionId,
        String multibancoEntity,
        String multibancoReference,
        String mbwayPhone,
        String paymentUrl,
        LocalDateTime referenceExpiresAt
) {
}