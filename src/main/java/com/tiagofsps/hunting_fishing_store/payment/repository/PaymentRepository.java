package com.tiagofsps.hunting_fishing_store.payment.repository;

import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
}
