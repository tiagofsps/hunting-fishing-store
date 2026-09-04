package com.tiagofsps.hunting_fishing_store.order.controller;

import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.order.dto.request.CheckoutRequest;
import com.tiagofsps.hunting_fishing_store.order.dto.response.OrderResponse;
import com.tiagofsps.hunting_fishing_store.order.service.OrderService;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final AuthorizationUtil authorizationUtil;

    public OrderController(OrderService orderService, UserService userService, AuthorizationUtil authorizationUtil) {
        this.orderService = orderService;
        this.userService = userService;
        this.authorizationUtil = authorizationUtil;
    }

    private void authorizeUserAccess(Long userId) {
        if (authorizationUtil.isAdmin()) {
            return;
        }
        
        var user = userService.findById(userId);
        if (!user.email().equals(authorizationUtil.getAuthenticatedUser())) {
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
    }

    @PostMapping("/checkout/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@PathVariable Long userId, @Valid @RequestBody CheckoutRequest request) {
        authorizeUserAccess(userId);
        return orderService.checkout(userId, request);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUserId(@PathVariable Long userId) {
        authorizeUserAccess(userId);
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getOrderByNumber(@PathVariable String orderNumber) {
        // For order detail view, we load the order and check if user owns it
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        
        if (!authorizationUtil.isAdmin()) {
            var user = userService.findById(order.userId());
            if (!user.email().equals(authorizationUtil.getAuthenticatedUser())) {
                throw new AccessDeniedException("You do not have permission to view this order");
            }
        }
        
        return order;
    }
}
