package com.tiagofsps.hunting_fishing_store.order.service;

import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.CartNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.OrderNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.coupon.service.CouponService;
import com.tiagofsps.hunting_fishing_store.order.dto.request.CheckoutRequest;
import com.tiagofsps.hunting_fishing_store.order.dto.response.OrderItemResponse;
import com.tiagofsps.hunting_fishing_store.order.dto.response.OrderResponse;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderItem;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;

    public OrderService(OrderRepository orderRepository, 
                        CartRepository cartRepository, 
                        UserRepository userRepository, 
                        CouponService couponService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.couponService = couponService;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductItem().getId(),
                        item.getProductItem().getProduct().getName(),
                        item.getProductItem().getSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingCost(),
                order.getUser().getId(),
                items
        );
    }

    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        Address shippingAddress = user.getAddresses().stream()
                .filter(a -> a.getId().equals(request.shippingAddressId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid shipping address"));

        Address billingAddress = user.getAddresses().stream()
                .filter(a -> a.getId().equals(request.billingAddressId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid billing address"));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductItem(cartItem.getProductItem());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProductItem().getPrice()); // Freeze price
            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        Coupon appliedCoupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            appliedCoupon = couponService.validateAndGet(request.couponCode(), userId, totalAmount);
            BigDecimal discount = couponService.calculateDiscount(appliedCoupon, totalAmount);
            order.setDiscountAmount(discount);
            totalAmount = totalAmount.subtract(discount);
        }

        order.setTotalAmount(totalAmount.max(BigDecimal.ZERO));

        Order savedOrder = orderRepository.save(order);

        if (appliedCoupon != null) {
            couponService.recordUsage(appliedCoupon, user, savedOrder);
        }

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }
}
