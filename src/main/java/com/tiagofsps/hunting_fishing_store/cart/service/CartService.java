package com.tiagofsps.hunting_fishing_store.cart.service;

import com.tiagofsps.hunting_fishing_store.cart.dto.request.AddToCartRequest;
import com.tiagofsps.hunting_fishing_store.cart.dto.response.CartItemResponse;
import com.tiagofsps.hunting_fishing_store.cart.dto.response.CartResponse;
import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductItemRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.CartNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.InsufficientStockException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductItemRepository productItemRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductItemRepository productItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productItemRepository = productItemRepository;
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProductItem().getId(),
                        item.getProductItem().getProduct().getName(),
                        item.getProductItem().getSku(),
                        item.getProductItem().getPrice(),
                        item.getQuantity(),
                        item.getProductItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUser().getId(), items, totalAmount);
    }

    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
        return toResponse(cart);
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        ProductItem productItem = productItemRepository.findById(request.productItemId())
                .orElseThrow(() -> new ProductNotFoundException(request.productItemId()));

        if (productItem.getStockQuantity() < request.quantity()) {
            throw new InsufficientStockException(productItem.getSku(), request.quantity(), productItem.getStockQuantity());
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductItem().getId().equals(request.productItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            int newQuantity = existingItem.get().getQuantity() + request.quantity();
            if (productItem.getStockQuantity() < newQuantity) {
                 throw new InsufficientStockException(productItem.getSku(), newQuantity, productItem.getStockQuantity());
            }
            existingItem.get().setQuantity(newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductItem(productItem);
            newItem.setQuantity(request.quantity());
            cart.getItems().add(newItem);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long productItemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductItem().getId().equals(productItemId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(productItemId));

        ProductItem productItem = item.getProductItem();
        if (productItem.getStockQuantity() < quantity) {
            throw new InsufficientStockException(productItem.getSku(), quantity, productItem.getStockQuantity());
        }

        item.setQuantity(quantity);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long productItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        cart.getItems().removeIf(item -> item.getProductItem().getId().equals(productItemId));
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
