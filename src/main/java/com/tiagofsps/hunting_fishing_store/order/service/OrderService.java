package com.tiagofsps.hunting_fishing_store.order.service;

import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.StockMovementRequest;
import com.tiagofsps.hunting_fishing_store.catalog.entity.LicenseType;
import com.tiagofsps.hunting_fishing_store.catalog.entity.MovementType;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.service.StockMovementService;
import com.tiagofsps.hunting_fishing_store.common.exception.CartNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.InsufficientStockException;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidOrderStatusTransitionException;
import com.tiagofsps.hunting_fishing_store.common.exception.LicenseRequiredException;
import com.tiagofsps.hunting_fishing_store.common.exception.OrderNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.mapper.AddressMapper;
import com.tiagofsps.hunting_fishing_store.common.service.MailService;
import com.tiagofsps.hunting_fishing_store.config.ShippingProperties;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class)
    );

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final StockMovementService stockMovementService;
    private final AddressMapper addressMapper;
    private final ShippingProperties shippingProperties;
    private final MailService mailService;

    public OrderService(OrderRepository orderRepository, 
                        CartRepository cartRepository, 
                        UserRepository userRepository, 
                        CouponService couponService,
                        StockMovementService stockMovementService,
                        AddressMapper addressMapper,
                        ShippingProperties shippingProperties,
                        MailService mailService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.couponService = couponService;
        this.stockMovementService = stockMovementService;
        this.addressMapper = addressMapper;
        this.shippingProperties = shippingProperties;
        this.mailService = mailService;
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

        BigDecimal subtotalAmount = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getStatus(),
                subtotalAmount,
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getShippingCost(),
                order.getUser().getId(),
                addressMapper.toResponse(order.getShippingAddress()),
                addressMapper.toResponse(order.getBillingAddress()),
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

        Address shippingAddress = resolveAddress(user, request.shippingAddressId(), "shipping");
        Address billingAddress = request.billingAddressId() == null
                ? shippingAddress
                : resolveAddress(user, request.billingAddressId(), "billing");

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            var productItem = cartItem.getProductItem();

            // Validate stock before persisting anything (deterministic failures).
            if (productItem.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        productItem.getProduct().getName(),
                        productItem.getSku(),
                        cartItem.getQuantity(),
                        productItem.getStockQuantity());
            }

            // License enforcement for regulated products.
            validateLicense(user, productItem.getProduct());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductItem(productItem);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(productItem.getPrice()); // Freeze price
            order.getItems().add(orderItem);

            subtotal = subtotal.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        // Shipping cost is derived from the subtotal BEFORE coupon discount.
        BigDecimal shippingCost = subtotal.compareTo(shippingProperties.getFreeThreshold()) >= 0
                ? BigDecimal.ZERO
                : shippingProperties.getFee();
        order.setShippingCost(shippingCost);

        Coupon appliedCoupon = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            appliedCoupon = couponService.validateAndGet(request.couponCode(), userId, subtotal);
            discount = couponService.calculateDiscount(appliedCoupon, subtotal);
            order.setDiscountAmount(discount);
        }

        order.setTotalAmount(subtotal.subtract(discount).add(shippingCost).max(BigDecimal.ZERO));

        Order savedOrder = orderRepository.save(order);

        // Record stock movements
        for (OrderItem item : savedOrder.getItems()) {
            stockMovementService.recordMovement(new StockMovementRequest(
                    item.getProductItem().getId(),
                    -item.getQuantity(),
                    MovementType.SALE,
                    "Order " + savedOrder.getOrderNumber(),
                    userId
            ));
        }

        if (appliedCoupon != null) {
            couponService.recordUsage(appliedCoupon, user, savedOrder);
        }

        // Best-effort confirmation mail (MailService swallows failures).
        mailService.send(user.getEmail(), "Order confirmation " + savedOrder.getOrderNumber(),
                "Thank you for your order " + savedOrder.getOrderNumber() + "!\n\n"
                        + "Total: " + savedOrder.getTotalAmount() + " EUR\n"
                        + "Status: " + savedOrder.getStatus() + "\n\n"
                        + "We will let you know when your order ships.");

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    private Address resolveAddress(User user, Long addressId, String label) {
        return user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid " + label + " address"));
    }

    private void validateLicense(User user, Product product) {
        if (!Boolean.TRUE.equals(product.getRequiresLicense())) {
            return;
        }

        boolean hasHuntingLicense = isNotBlank(user.getHuntingLicenseNumber());
        boolean hasFishingLicense = isNotBlank(user.getFishingLicenseNumber());

        boolean satisfied = switch (product.getLicenseType()) {
            case HUNTING -> hasHuntingLicense;
            case FISHING -> hasFishingLicense;
            // Legacy data may have requiresLicense=true with no licenseType set;
            // accept either license type for those products.
            case null -> hasHuntingLicense || hasFishingLicense;
        };

        if (!satisfied) {
            throw new LicenseRequiredException(product.getName());
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    /**
     * Admin-only dashboard list. Optional status filter; mapping reuses the
     * enriched toResponse shape.
     */
    public Page<OrderResponse> findAll(OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findAllByStatus(status, pageable);
        return orders.map(this::toResponse);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }

    /**
     * Single entry point for all order status transitions. Enforces the
     * allowed transition map. Cancelling an order records reverse stock
     * movements (RETURN) for every line item.
     */
    @Transactional
    public Order transitionStatus(Order order, OrderStatus target) {
        if (order == null || order.getId() == null) {
            throw new InvalidOrderStatusTransitionException("Cannot transition status of an unsaved order");
        }

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.get(order.getStatus());
        if (allowed == null || !allowed.contains(target)) {
            throw new InvalidOrderStatusTransitionException(order.getStatus(), target);
        }

        order.setStatus(target);

        if (target == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                stockMovementService.recordMovement(new StockMovementRequest(
                        item.getProductItem().getId(),
                        item.getQuantity(),
                        MovementType.RETURN,
                        "Order " + order.getOrderNumber() + " cancelled",
                        order.getUser().getId()
                ));
            }
        }

        return orderRepository.save(order);
    }

    @Transactional
    public OrderResponse updateStatus(String orderNumber, OrderStatus target) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        transitionStatus(order, target);
        return toResponse(order);
    }
}