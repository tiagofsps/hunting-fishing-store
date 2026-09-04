package com.tiagofsps.hunting_fishing_store.cart.controller;

import com.tiagofsps.hunting_fishing_store.cart.dto.request.AddToCartRequest;
import com.tiagofsps.hunting_fishing_store.cart.dto.response.CartResponse;
import com.tiagofsps.hunting_fishing_store.cart.service.CartService;
import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final AuthorizationUtil authorizationUtil;

    public CartController(CartService cartService, UserService userService, AuthorizationUtil authorizationUtil) {
        this.cartService = cartService;
        this.userService = userService;
        this.authorizationUtil = authorizationUtil;
    }

    private void authorizeUserAccess(Long userId) {
        if (authorizationUtil.isAdmin()) {
            return;
        }
        
        var user = userService.findById(userId);
        if (!user.email().equals(authorizationUtil.getAuthenticatedUser())) {
            throw new AccessDeniedException("You do not have permission to access this cart");
        }
    }

    @GetMapping("/{userId}")
    public CartResponse getCart(@PathVariable Long userId) {
        authorizeUserAccess(userId);
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(@PathVariable Long userId, @Valid @RequestBody AddToCartRequest request) {
        authorizeUserAccess(userId);
        return cartService.addToCart(userId, request);
    }

    @PutMapping("/{userId}/items/{productItemId}")
    public CartResponse updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productItemId,
            @RequestParam Integer quantity) {
        authorizeUserAccess(userId);
        return cartService.updateQuantity(userId, productItemId, quantity);
    }

    @DeleteMapping("/{userId}/items/{productItemId}")
    public CartResponse removeFromCart(@PathVariable Long userId, @PathVariable Long productItemId) {
        authorizeUserAccess(userId);
        return cartService.removeFromCart(userId, productItemId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable Long userId) {
        authorizeUserAccess(userId);
        cartService.clearCart(userId);
    }
}
