package com.tiagofsps.hunting_fishing_store.cart.service;

import com.tiagofsps.hunting_fishing_store.cart.dto.request.AddToCartRequest;
import com.tiagofsps.hunting_fishing_store.cart.dto.response.CartResponse;
import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductItemRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.InsufficientStockException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private ProductItem productItem;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        Product product = new Product();
        product.setName("Rod");

        productItem = new ProductItem();
        productItem.setId(1L);
        productItem.setSku("ROD-001");
        productItem.setPrice(BigDecimal.valueOf(100.0));
        productItem.setStockQuantity(10);
        productItem.setProduct(product);
    }

    @Test
    @DisplayName("Should add item to cart when stock is sufficient")
    void shouldAddItemToCart() {
        AddToCartRequest request = new AddToCartRequest(1L, 2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addToCart(1L, request);

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().getFirst().quantity());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw exception when stock is insufficient")
    void shouldThrowWhenStockInsufficient() {
        AddToCartRequest request = new AddToCartRequest(1L, 20);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItem));

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(1L, request));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update quantity of existing item")
    void shouldUpdateQuantityOfExistingItem() {
        CartItem existingItem = new CartItem();
        existingItem.setProductItem(productItem);
        existingItem.setQuantity(2);
        cart.getItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest(1L, 3);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addToCart(1L, request);

        assertEquals(1, response.items().size());
        assertEquals(5, response.items().getFirst().quantity());
    }
}
