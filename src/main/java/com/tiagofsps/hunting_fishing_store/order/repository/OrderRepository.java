package com.tiagofsps.hunting_fishing_store.order.repository;

import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
    boolean existsByShippingAddressIdOrBillingAddressId(Long shippingAddressId, Long billingAddressId);
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
}
