package com.tiagofsps.hunting_fishing_store.order.controller;

import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.order.dto.request.CheckoutRequest;
import com.tiagofsps.hunting_fishing_store.order.dto.response.OrderResponse;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order checkout and management")
public class OrderController {

    private final OrderService orderService;
    private final AuthorizationUtil authorizationUtil;

    public OrderController(OrderService orderService, AuthorizationUtil authorizationUtil) {
        this.orderService = orderService;
        this.authorizationUtil = authorizationUtil;
    }

    private void authorizeUserAccess(Long userId) {
        if (!authorizationUtil.isAuthorizedToAccess(userId, null)) {
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
    }

    @PostMapping("/checkout/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order from the current cart")
    public OrderResponse checkout(@PathVariable Long userId, @Valid @RequestBody CheckoutRequest request) {
        authorizeUserAccess(userId);
        return orderService.checkout(userId, request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Paginated order list for the dashboard (optional status filter)")
    public Page<OrderResponse> findAll(@PageableDefault(size = 20) Pageable pageable,
                                       @RequestParam(required = false) OrderStatus status) {
        return orderService.findAll(status, pageable);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List a user's order history (owner or ADMIN)")
    public List<OrderResponse> getOrdersByUserId(@PathVariable Long userId) {
        authorizeUserAccess(userId);
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Get order details by order number (owner or ADMIN)")
    public OrderResponse getOrderByNumber(@PathVariable String orderNumber) {
        // For order detail view, we load the order and check if user owns it
        OrderResponse order = orderService.getOrderByNumber(orderNumber);

        if (!authorizationUtil.isAuthorizedToAccess(order.userId(), null)) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return order;
    }

    @PatchMapping("/{orderNumber}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Transition an order status")
    public OrderResponse updateStatus(@PathVariable String orderNumber, @RequestParam OrderStatus status) {
        return orderService.updateStatus(orderNumber, status);
    }
}