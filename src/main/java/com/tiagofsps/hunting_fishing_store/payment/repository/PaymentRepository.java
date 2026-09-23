package com.tiagofsps.hunting_fishing_store.payment.repository;

import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
    boolean existsByMultibancoReference(String multibancoReference);
    List<Payment> findAllByStatusAndPaymentMethodAndReferenceExpiresAtBefore(PaymentStatus status, PaymentMethod method, LocalDateTime referenceExpiresAt);
}