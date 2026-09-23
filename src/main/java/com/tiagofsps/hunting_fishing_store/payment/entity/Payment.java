package com.tiagofsps.hunting_fishing_store.payment.entity;

import com.tiagofsps.hunting_fishing_store.common.BaseEntity;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String transactionId;

    private String multibancoEntity;

    @Column(unique = true)
    private String multibancoReference;

    private String mbwayPhone;

    private String paymentUrl;

    private LocalDateTime referenceExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}