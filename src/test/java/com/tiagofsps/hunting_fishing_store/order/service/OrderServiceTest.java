package com.tiagofsps.hunting_fishing_store.order.service;

import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
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
    }
}
