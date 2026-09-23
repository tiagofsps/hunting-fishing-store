package com.tiagofsps.hunting_fishing_store.payment.service;

import com.tiagofsps.hunting_fishing_store.common.exception.InvalidPaymentRequestException;
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
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest mbwayRequest() {
        return new PaymentRequest(1L, PaymentMethod.MBWAY, "351912345678");
    }

    private Order orderWithPendingStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(50.0));
        return order;
    }

    private Payment pendingPayment(Order order) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount());
        return payment;
    }

    @Test
    @DisplayName("Should route PENDING order to PAID via transition when payment completes")
    void shouldUpdateOrderToPaid() {
        Order order = orderWithPendingStatus();
        Payment payment = pendingPayment(order);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        doAnswer(invocation -> {
            Order target = invocation.getArgument(0);
            target.setStatus(invocation.getArgument(1));
            return target;
        }).when(orderService).transitionStatus(order, OrderStatus.PAID);

        paymentService.updateStatus(1L, PaymentStatus.COMPLETED);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderService).transitionStatus(order, OrderStatus.PAID);
    }

    @Test
    @DisplayName("Should not change order status when payment is not COMPLETED")
    void shouldNotChangeOrderStatusForNonCompletedPayment() {
        Order order = orderWithPendingStatus();
        Payment payment = pendingPayment(order);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.updateStatus(1L, PaymentStatus.FAILED);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(orderService, never()).transitionStatus(any(), any());
    }

    @Test
    @DisplayName("Should initiate a new payment with amount read from the order")
    void shouldInitiatePaymentWithAmountFromOrder() {
        Order order = orderWithPendingStatus();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authorizationUtil.isAdmin()).thenReturn(true);
        when(paymentRepository.findByOrderIdAndStatus(1L, PaymentStatus.PENDING)).thenReturn(Optional.empty());
        when(paymentGateway.initiate(any(Payment.class))).thenReturn(new GatewayResult(null, null, null, null));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.initiatePayment(mbwayRequest());

        assertEquals(1L, response.orderId());
        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(BigDecimal.valueOf(50.0), response.amount());
        assertEquals("351912345678", response.mbwayPhone());
        assertNotNull(response.transactionId());
    }

    @Test
    @DisplayName("Should reject MBWAY payment without a phone number")
    void shouldRejectMbwayWithoutPhone() {
        Order order = orderWithPendingStatus();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authorizationUtil.isAdmin()).thenReturn(true);
        when(paymentRepository.findByOrderIdAndStatus(1L, PaymentStatus.PENDING)).thenReturn(Optional.empty());

        PaymentRequest request = new PaymentRequest(1L, PaymentMethod.MBWAY, "  ");

        assertThrows(InvalidPaymentRequestException.class, () -> paymentService.initiatePayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should generate and persist Multibanco reference and expiry, retrying on collision")
    void shouldGenerateMultibancoReferenceWithRetryOnCollision() {
        Order order = orderWithPendingStatus();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authorizationUtil.isAdmin()).thenReturn(true);
        when(paymentRepository.findByOrderIdAndStatus(1L, PaymentStatus.PENDING)).thenReturn(Optional.empty());

        GatewayResult first = new GatewayResult(null, "21345", "111111111", LocalDateTime.now().plusHours(48));
        GatewayResult second = new GatewayResult(null, "21345", "222222222", LocalDateTime.now().plusHours(48));
        when(paymentGateway.initiate(any(Payment.class))).thenReturn(first, second);
        when(paymentRepository.existsByMultibancoReference("111111111")).thenReturn(true);
        when(paymentRepository.existsByMultibancoReference("222222222")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.initiatePayment(new PaymentRequest(1L, PaymentMethod.MULTIBANCO, null));

        assertEquals("222222222", response.multibancoReference());
        assertEquals("21345", response.multibancoEntity());
        assertNotNull(response.referenceExpiresAt());
        verify(paymentGateway, times(2)).initiate(any(Payment.class));
    }

    @Test
    @DisplayName("Should set a payment URL placeholder for card-based payments")
    void shouldSetPaymentUrlForCardAndKlarna() {
        Order order = orderWithPendingStatus();
        order.setOrderNumber("ORD-ABC123");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authorizationUtil.isAdmin()).thenReturn(true);
        when(paymentRepository.findByOrderIdAndStatus(1L, PaymentStatus.PENDING)).thenReturn(Optional.empty());
        when(paymentGateway.initiate(any(Payment.class))).thenReturn(new GatewayResult("http://checkout/checkout/order/ORD-ABC123", null, null, null));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.initiatePayment(new PaymentRequest(1L, PaymentMethod.KLARNA, null));

        assertEquals("http://checkout/checkout/order/ORD-ABC123", response.paymentUrl());
    }

    @Test
    @DisplayName("Should deny initiation when caller does not own the order")
    void shouldDenyForeignUserInitiation() {
        Order order = orderWithPendingStatus();
        User owner = new User();
        owner.setEmail("owner@example.com");
        order.setUser(owner);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authorizationUtil.isAdmin()).thenReturn(false);
        when(authorizationUtil.getAuthenticatedUser()).thenReturn("other@example.com");

        assertThrows(AccessDeniedException.class, () -> paymentService.initiatePayment(mbwayRequest()));
    }

    @Test
    @DisplayName("Should reject initiation when order is not PENDING")
    void shouldRejectWhenOrderNotPending() {
        Order order = orderWithPendingStatus();
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authorizationUtil.isAdmin()).thenReturn(true);

        assertThrows(PaymentStateException.class, () -> paymentService.initiatePayment(mbwayRequest()));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject initiation when a pending payment already exists")
    void shouldRejectWhenPendingPaymentExists() {
        Order order = orderWithPendingStatus();
        Payment existing = pendingPayment(order);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authorizationUtil.isAdmin()).thenReturn(true);
        when(paymentRepository.findByOrderIdAndStatus(1L, PaymentStatus.PENDING)).thenReturn(Optional.of(existing));

        assertThrows(PaymentStateException.class, () -> paymentService.initiatePayment(mbwayRequest()));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw when payment not found")
    void shouldThrowWhenNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.findById(1L));
    }

    @Test
    @DisplayName("Should deny payment read when caller does not own the order")
    void shouldDenyForeignUserRead() {
        Order order = orderWithPendingStatus();
        User owner = new User();
        owner.setEmail("owner@example.com");
        order.setUser(owner);
        Payment payment = pendingPayment(order);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(authorizationUtil.isAdmin()).thenReturn(false);
        when(authorizationUtil.getAuthenticatedUser()).thenReturn("other@example.com");

        assertThrows(AccessDeniedException.class, () -> paymentService.findById(1L));
    }

    @Test
    @DisplayName("Should complete payment and transition order to PAID via webhook")
    void shouldCompletePaymentFromWebhook() {
        Order order = orderWithPendingStatus();
        Payment payment = pendingPayment(order);

        when(paymentGateway.handleWebhook("payload", "sig")).thenReturn(new WebhookResult(1L, WebhookOutcome.SUCCESS));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse response = paymentService.handleWebhook("payload", "sig");

        assertEquals(PaymentStatus.COMPLETED, response.status());
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        verify(orderService).transitionStatus(order, OrderStatus.PAID);
    }

    @Test
    @DisplayName("Should mark payment FAILED on failure outcome")
    void shouldFailPaymentFromWebhook() {
        Order order = orderWithPendingStatus();
        Payment payment = pendingPayment(order);

        when(paymentGateway.handleWebhook("payload", "sig")).thenReturn(new WebhookResult(1L, WebhookOutcome.FAILURE));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse response = paymentService.handleWebhook("payload", "sig");

        assertEquals(PaymentStatus.FAILED, response.status());
        verify(orderService, never()).transitionStatus(any(), any());
    }

    @Test
    @DisplayName("Should be idempotent when webhook redelivers after completion")
    void shouldBeIdempotentAfterCompletion() {
        Order order = orderWithPendingStatus();
        order.setStatus(OrderStatus.PAID);
        Payment payment = pendingPayment(order);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentGateway.handleWebhook("payload", "sig")).thenReturn(new WebhookResult(1L, WebhookOutcome.SUCCESS));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.handleWebhook("payload", "sig");

        assertEquals(PaymentStatus.COMPLETED, response.status());
        verify(orderService, never()).transitionStatus(any(), any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw when webhook references an unknown payment")
    void shouldThrowWhenWebhookPaymentUnknown() {
        when(paymentGateway.handleWebhook("payload", "sig")).thenReturn(new WebhookResult(999L, WebhookOutcome.SUCCESS));
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.handleWebhook("payload", "sig"));
    }

    @Test
    @DisplayName("Should expire overdue PENDING Multibanco references")
    void shouldExpireOverdueReferences() {
        Order order = orderWithPendingStatus();
        Payment overdue = new Payment();
        overdue.setId(1L);
        overdue.setOrder(order);
        overdue.setStatus(PaymentStatus.PENDING);
        overdue.setPaymentMethod(PaymentMethod.MULTIBANCO);
        overdue.setReferenceExpiresAt(LocalDateTime.now().minusMinutes(5));

        when(paymentRepository.findAllByStatusAndPaymentMethodAndReferenceExpiresAtBefore(
                eq(PaymentStatus.PENDING), eq(PaymentMethod.MULTIBANCO), any(LocalDateTime.class)))
                .thenReturn(List.of(overdue));
        when(paymentRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        paymentService.expireOverdueReferences();

        assertEquals(PaymentStatus.EXPIRED, overdue.getStatus());
        verify(paymentRepository).saveAll(List.of(overdue));
    }
}