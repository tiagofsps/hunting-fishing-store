package com.tiagofsps.hunting_fishing_store.order.repository;

import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.AddressRepository;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest
class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldSaveOrder() {
        User user = new User();
        user.setEmail("order@example.com");
        user.setPassword("pass");
        user.setFirstName("O");
        user.setLastName("U");
        userRepository.save(user);

        Address address = new Address();
        address.setRecipientName("John");
        address.setRecipientPhone("123");
        address.setStreet("Street");
        address.setCity("City");
        address.setPostalCode("123");
        address.setCountryCode("PT");
        address.setAddressType(AddressType.SHIPPING);
        address.setUser(user);
        addressRepository.save(address);

        Order order = new Order();
        order.setOrderNumber("ORD-TEST");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setUser(user);
        order.setShippingAddress(address);
        order.setBillingAddress(address);

        Order saved = orderRepository.save(order);

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindOrderByNumber() {
        // Setup similar to above...
        User user = new User();
        user.setEmail("order2@example.com");
        user.setPassword("pass");
        user.setFirstName("O");
        user.setLastName("U");
        userRepository.save(user);

        Address address = new Address();
        address.setRecipientName("John");
        address.setRecipientPhone("123");
        address.setStreet("Street");
        address.setCity("City");
        address.setPostalCode("123");
        address.setCountryCode("PT");
        address.setAddressType(AddressType.SHIPPING);
        address.setUser(user);
        addressRepository.save(address);

        Order order = new Order();
        order.setOrderNumber("ORD-12345");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setUser(user);
        order.setShippingAddress(address);
        order.setBillingAddress(address);
        orderRepository.save(order);

        Optional<Order> result = orderRepository.findByOrderNumber("ORD-12345");

        assertTrue(result.isPresent());
    }
}
