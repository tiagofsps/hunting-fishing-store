package com.tiagofsps.hunting_fishing_store.payment.repository;

import com.tiagofsps.hunting_fishing_store.BaseRepositoryTest;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.payment.entity.Payment;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentStatus;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.AddressRepository;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldSavePayment() {
        User user = new User();
        user.setEmail("pay@example.com");
        user.setPassword("pass");
        user.setFirstName("P");
        user.setLastName("U");
        userRepository.save(user);

        Address address = new Address();
        address.setRecipientName("R");
        address.setRecipientPhone("1");
        address.setStreet("S");
        address.setCity("C");
        address.setPostalCode("P");
        address.setCountryCode("PT");
        address.setAddressType(AddressType.SHIPPING);
        address.setUser(user);
        addressRepository.save(address);

        Order order = new Order();
        order.setOrderNumber("ORD-PAY");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setUser(user);
        order.setShippingAddress(address);
        order.setBillingAddress(address);
        orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(BigDecimal.valueOf(100.0));
        payment.setPaymentMethod(PaymentMethod.MBWAY);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId("tx-123");

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved.getId());
    }
}
