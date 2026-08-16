package com.tiagofsps.hunting_fishing_store.payment.service;

import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentStatus;
import com.tiagofsps.hunting_fishing_store.payment.repository.PaymentRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.PaymentNotFoundException;
import com.tiagofsps.hunting_fishing_store.payment.dto.request.PaymentRequest;
import com.tiagofsps.hunting_fishing_store.payment.dto.response.PaymentResponse;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Should update order status to PAID when payment is COMPLETED")
    void shouldUpdateOrderToPaid() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.updateStatus(1L, PaymentStatus.COMPLETED);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should initiate a new payment")
    void shouldInitiatePayment() {
        Order order = new Order();
        order.setId(1L);
        PaymentRequest request = new PaymentRequest(1L, PaymentMethod.MBWAY, java.math.BigDecimal.valueOf(50.0));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.initiatePayment(request);

        assertEquals(1L, response.orderId());
        assertEquals(PaymentStatus.PENDING, response.status());
        assertNotNull(response.transactionId());
    }

    @Test
    @DisplayName("Should throw when payment not found")
    void shouldThrowWhenNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.findById(1L));
    }
}
