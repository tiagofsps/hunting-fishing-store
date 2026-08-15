package com.tiagofsps.hunting_fishing_store.cart.controller;

import com.tiagofsps.hunting_fishing_store.cart.dto.request.AddToCartRequest;
import com.tiagofsps.hunting_fishing_store.cart.dto.response.CartResponse;
import com.tiagofsps.hunting_fishing_store.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public CartResponse getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(@PathVariable Long userId, @Valid @RequestBody AddToCartRequest request) {
        return cartService.addToCart(userId, request);
    }

    @PutMapping("/{userId}/items/{productItemId}")
    public CartResponse updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productItemId,
            @RequestParam Integer quantity) {
        return cartService.updateQuantity(userId, productItemId, quantity);
    }

    @DeleteMapping("/{userId}/items/{productItemId}")
    public CartResponse removeFromCart(@PathVariable Long userId, @PathVariable Long productItemId) {
        return cartService.removeFromCart(userId, productItemId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
    }
}
