package com.tiagofsps.hunting_fishing_store.order.service;

import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.service.StockMovementService;
import com.tiagofsps.hunting_fishing_store.common.exception.OrderNotFoundException;
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.coupon.service.CouponService;
import com.tiagofsps.hunting_fishing_store.order.dto.request.CheckoutRequest;
import com.tiagofsps.hunting_fishing_store.order.dto.response.OrderResponse;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Address address;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        
        address = new Address();
        address.setId(1L);
        user.setAddresses(List.of(address));

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        Product product = new Product();
        product.setName("Rod");

        ProductItem item = new ProductItem();
        item.setId(1L);
        item.setPrice(BigDecimal.valueOf(100.0));
        item.setProduct(product);

        CartItem cartItem = new CartItem();
        cartItem.setProductItem(item);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);
    }

    @Test
    @DisplayName("Should checkout cart and create order")
    void shouldCheckoutCart() {
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse response = orderService.checkout(1L, request);

        assertNotNull(response.orderNumber());
        assertEquals(BigDecimal.valueOf(200.0), response.totalAmount());
        assertTrue(cart.getItems().isEmpty());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart);
        verify(stockMovementService, times(1)).recordMovement(any());
    }

    @Test
    @DisplayName("Should throw when checkout an empty cart")
    void shouldThrowWhenEmptyCart() {
        cart.getItems().clear();
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalStateException.class, () -> orderService.checkout(1L, request));
    }

    @Test
    @DisplayName("Should throw when address is invalid")
    void shouldThrowWhenInvalidAddress() {
        CheckoutRequest request = new CheckoutRequest(99L, 1L, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(1L, request));
    }

    @Test
    @DisplayName("Should checkout with coupon")
    void shouldCheckoutWithCoupon() {
        CheckoutRequest request = new CheckoutRequest(1L, 1L, "SAVE10");
        Coupon coupon = new Coupon();
        coupon.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(couponService.validateAndGet("SAVE10", 1L, BigDecimal.valueOf(200.0))).thenReturn(coupon);
        when(couponService.calculateDiscount(any(), any())).thenReturn(BigDecimal.valueOf(20.0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.checkout(1L, request);

        assertEquals(0, response.totalAmount().compareTo(BigDecimal.valueOf(180.0)));
        verify(couponService).recordUsage(eq(coupon), eq(user), any());
    }

    @Test
    @DisplayName("Should return order by number")
    void shouldGetOrderByNumber() {
        Order order = new Order();
        order.setOrderNumber("ORD-1");
        order.setUser(user);
        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderByNumber("ORD-1");

        assertEquals("ORD-1", response.orderNumber());
    }

    @Test
    @DisplayName("Should throw when order number not found")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findByOrderNumber("XXX")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderByNumber("XXX"));
    }
}
