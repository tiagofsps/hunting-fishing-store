package com.tiagofsps.hunting_fishing_store.payment.service;

import com.tiagofsps.hunting_fishing_store.common.exception.InvalidPaymentRequestException;
import com.tiagofsps.hunting_fishing_store.common.exception.OrderNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.PaymentNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.PaymentStateException;
import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.order.service.OrderService;
import com.tiagofsps.hunting_fishing_store.payment.dto.request.PaymentRequest;
import com.tiagofsps.hunting_fishing_store.payment.dto.response.PaymentResponse;
import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentStatus;
import com.tiagofsps.hunting_fishing_store.payment.gateway.GatewayResult;
import com.tiagofsps.hunting_fishing_store.payment.gateway.PaymentGateway;
import com.tiagofsps.hunting_fishing_store.payment.gateway.WebhookOutcome;
import com.tiagofsps.hunting_fishing_store.payment.gateway.WebhookResult;
import com.tiagofsps.hunting_fishing_store.payment.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final int REFERENCE_RETRY_ATTEMPTS = 5;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AuthorizationUtil authorizationUtil;
    private final OrderService orderService;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, AuthorizationUtil authorizationUtil, OrderService orderService, PaymentGateway paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.authorizationUtil = authorizationUtil;
        this.orderService = orderService;
        this.paymentGateway = paymentGateway;
    }

    private void authorizeOrderAccess(Order order) {
        if (authorizationUtil.isAdmin()) {
            return;
        }

        String authenticatedUser = authorizationUtil.getAuthenticatedUser();
        if (authenticatedUser == null || !order.getUser().getEmail().equals(authenticatedUser)) {
            throw new AccessDeniedException("You do not have permission to access this payment");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getMultibancoEntity(),
                payment.getMultibancoReference(),
                payment.getMbwayPhone(),
                payment.getPaymentUrl(),
                payment.getReferenceExpiresAt()
        );
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

        authorizeOrderAccess(order);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentStateException("Cannot initiate payment: order status is " + order.getStatus());
        }

        boolean hasPendingPayment = paymentRepository.findByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING).isPresent();
        if (hasPendingPayment) {
            throw new PaymentStateException("A pending payment already exists for this order");
        }

        if (request.paymentMethod() == PaymentMethod.MBWAY && isBlank(request.mbwayPhone())) {
            throw new InvalidPaymentRequestException("mbwayPhone is required for MBWAY payments");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setMbwayPhone(request.mbwayPhone());

        applyGatewayResult(payment);

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    private void applyGatewayResult(Payment payment) {
        GatewayResult result = paymentGateway.initiate(payment);

        for (int attempt = 0;
             attempt < REFERENCE_RETRY_ATTEMPTS
                     && result.multibancoReference() != null
                     && paymentRepository.existsByMultibancoReference(result.multibancoReference());
             attempt++) {
            result = paymentGateway.initiate(payment);
        }

        if (result.multibancoReference() != null
                && paymentRepository.existsByMultibancoReference(result.multibancoReference())) {
            throw new PaymentStateException("Could not generate a unique Multibanco reference");
        }

        payment.setPaymentUrl(result.paymentUrl());
        payment.setMultibancoEntity(result.multibancoEntity());
        payment.setMultibancoReference(result.multibancoReference());
        payment.setReferenceExpiresAt(result.referenceExpiresAt());
    }

    @Transactional
    public PaymentResponse updateStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.setStatus(status);

        if (status == PaymentStatus.COMPLETED) {
            orderService.transitionStatus(payment.getOrder(), OrderStatus.PAID);
        }

        Payment updated = paymentRepository.save(payment);
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        authorizeOrderAccess(payment.getOrder());
        return toResponse(payment);
    }

    /**
     * Public webhook entry point. The gateway stub verifies the signature and
     * parses the provider payload; here we load the payment and apply the
     * outcome. Re-deliveries after a successful completion are idempotent.
     */
    @Transactional
    public PaymentResponse handleWebhook(String payload, String signatureHeader) {
        WebhookResult webhookResult = paymentGateway.handleWebhook(payload, signatureHeader);

        Payment payment = paymentRepository.findById(webhookResult.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(webhookResult.paymentId()));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return toResponse(payment);
        }

        if (webhookResult.outcome() == WebhookOutcome.SUCCESS) {
            if (payment.getStatus() != PaymentStatus.PENDING) {
                throw new PaymentStateException("Cannot complete payment in status " + payment.getStatus());
            }
            payment.setStatus(PaymentStatus.COMPLETED);
            orderService.transitionStatus(payment.getOrder(), OrderStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return toResponse(paymentRepository.save(payment));
    }

    /**
     * Marks overdue PENDING Multibanco payments as EXPIRED on the configured
     * schedule (default hourly). Kept minimal: no email, no state machine.
     */
    @Scheduled(cron = "${app.payment.expiry-cron:0 0 * * * *}")
    @Transactional
    public void expireOverdueReferences() {
        List<Payment> overdue = paymentRepository.findAllByStatusAndPaymentMethodAndReferenceExpiresAtBefore(
                PaymentStatus.PENDING, PaymentMethod.MULTIBANCO, java.time.LocalDateTime.now());
        overdue.forEach(payment -> payment.setStatus(PaymentStatus.EXPIRED));
        paymentRepository.saveAll(overdue);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}