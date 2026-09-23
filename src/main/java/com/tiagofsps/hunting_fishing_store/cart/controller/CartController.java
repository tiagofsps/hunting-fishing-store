package com.tiagofsps.hunting_fishing_store.cart.controller;

import com.tiagofsps.hunting_fishing_store.cart.dto.request.AddToCartRequest;
import com.tiagofsps.hunting_fishing_store.cart.dto.response.CartResponse;
import com.tiagofsps.hunting_fishing_store.cart.service.CartService;
import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;
    private final AuthorizationUtil authorizationUtil;

    public CartController(CartService cartService, AuthorizationUtil authorizationUtil) {
        this.cartService = cartService;
        this.authorizationUtil = authorizationUtil;
    }

    private void authorizeUserAccess(Long userId) {
        if (!authorizationUtil.isAuthorizedToAccess(userId, null)) {
            throw new AccessDeniedException("You do not have permission to access this cart");
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Retrieve a user's cart")
    public CartResponse getCart(@PathVariable Long userId) {
        authorizeUserAccess(userId);
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an item to the cart")
    public CartResponse addToCart(@PathVariable Long userId, @Valid @RequestBody AddToCartRequest request) {
        authorizeUserAccess(userId);
        return cartService.addToCart(userId, request);
    }

    @PutMapping("/{userId}/items/{productItemId}")
    @Operation(summary = "Update the quantity of a cart item")
    public CartResponse updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productItemId,
            @RequestParam Integer quantity) {
        authorizeUserAccess(userId);
        return cartService.updateQuantity(userId, productItemId, quantity);
    }

    @DeleteMapping("/{userId}/items/{productItemId}")
    @Operation(summary = "Remove an item from the cart")
    public CartResponse removeFromCart(@PathVariable Long userId, @PathVariable Long productItemId) {
        authorizeUserAccess(userId);
        return cartService.removeFromCart(userId, productItemId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Clear the entire cart")
    public void clearCart(@PathVariable Long userId) {
        authorizeUserAccess(userId);
        cartService.clearCart(userId);
    }
}
