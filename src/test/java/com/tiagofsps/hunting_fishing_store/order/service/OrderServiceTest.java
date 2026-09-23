package com.tiagofsps.hunting_fishing_store.order.service;

import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.entity.LicenseType;
import com.tiagofsps.hunting_fishing_store.catalog.entity.MovementType;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.service.StockMovementService;
import com.tiagofsps.hunting_fishing_store.common.exception.InsufficientStockException;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidOrderStatusTransitionException;
import com.tiagofsps.hunting_fishing_store.common.exception.LicenseRequiredException;
import com.tiagofsps.hunting_fishing_store.common.exception.OrderNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.mapper.AddressMapper;
import com.tiagofsps.hunting_fishing_store.common.service.MailService;
import com.tiagofsps.hunting_fishing_store.config.ShippingProperties;
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.coupon.service.CouponService;
import com.tiagofsps.hunting_fishing_store.order.dto.request.CheckoutRequest;
import com.tiagofsps.hunting_fishing_store.order.dto.response.OrderResponse;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderItem;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private MailService mailService;

    private OrderService orderService;

    private User user;
    private Cart cart;
    private Address address;

    @BeforeEach
    void setup() {
        orderService = new OrderService(orderRepository, cartRepository, userRepository,
                couponService, stockMovementService, new AddressMapper(), new ShippingProperties(), mailService);

        user = new User();
        user.setId(1L);

        address = new Address();
        address.setId(1L);
        user.setAddresses(List.of(address));

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        setCartLine(BigDecimal.valueOf(100.0), 2, 10);
    }

    private void setCartLine(BigDecimal price, int quantity, int stock) {
        cart.getItems().clear();

        Product product = new Product();
        product.setName("Rod");

        ProductItem item = new ProductItem();
        item.setId(1L);
        item.setSku("SKU-1");
        item.setPrice(price);
        item.setStockQuantity(stock);
        item.setProduct(product);

        CartItem cartItem = new CartItem();
        cartItem.setProductItem(item);
        cartItem.setQuantity(quantity);
        cart.getItems().add(cartItem);

        user.setHuntingLicenseNumber(null);
        user.setFishingLicenseNumber(null);
        product.setRequiresLicense(false);
        product.setLicenseType(null);
    }

    private void stubStandardCheckout() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    }

    private void stubOrderSave() {
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Should checkout cart and create order")
    void shouldCheckoutCart() {
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();
        stubOrderSave();

        OrderResponse response = orderService.checkout(1L, request);

        assertNotNull(response.orderNumber());
        assertEquals(BigDecimal.valueOf(200.0), response.totalAmount());
        assertEquals(0, response.shippingCost().compareTo(BigDecimal.ZERO));
        assertTrue(cart.getItems().isEmpty());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart);
        verify(stockMovementService, times(1)).recordMovement(any());
    }

    @Test
    @DisplayName("Should apply shipping fee below the free threshold")
    void shouldApplyShippingFeeBelowThreshold() {
        setCartLine(BigDecimal.valueOf(50.00), 1, 10);
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();
        stubOrderSave();

        OrderResponse response = orderService.checkout(1L, request);

        assertEquals(0, response.shippingCost().compareTo(new BigDecimal("4.95")));
        assertEquals(0, response.totalAmount().compareTo(new BigDecimal("54.95")));
    }

    @Test
    @DisplayName("Should apply free shipping exactly at the threshold")
    void shouldApplyFreeShippingAtThreshold() {
        setCartLine(BigDecimal.valueOf(75.00), 1, 10);
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();
        stubOrderSave();

        OrderResponse response = orderService.checkout(1L, request);

        assertEquals(0, response.shippingCost().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.totalAmount().compareTo(BigDecimal.valueOf(75.00)));
    }

    @Test
    @DisplayName("Should include shipping fee in total after coupon discount")
    void shouldIncludeShippingFeeAfterDiscount() {
        setCartLine(BigDecimal.valueOf(50.00), 1, 10);
        CheckoutRequest request = new CheckoutRequest(1L, 1L, "SAVE10");

        Coupon coupon = new Coupon();
        coupon.setId(1L);

        stubStandardCheckout();
        when(couponService.validateAndGet(eq("SAVE10"), eq(1L), any(BigDecimal.class))).thenReturn(coupon);
        when(couponService.calculateDiscount(any(), any())).thenReturn(new BigDecimal("10.00"));
        stubOrderSave();

        OrderResponse response = orderService.checkout(1L, request);

        assertEquals(0, response.discountAmount().compareTo(new BigDecimal("10.00")));
        assertEquals(0, response.shippingCost().compareTo(new BigDecimal("4.95")));
        assertEquals(0, response.totalAmount().compareTo(new BigDecimal("44.95")));
    }

    @Test
    @DisplayName("Should reject checkout when stock is insufficient before persisting")
    void shouldRejectInsufficientStockBeforeSave() {
        setCartLine(BigDecimal.valueOf(100.0), 5, 3);
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();

        assertThrows(InsufficientStockException.class, () -> orderService.checkout(1L, request));

        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        verify(stockMovementService, never()).recordMovement(any());
        assertFalse(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should default billing address to shipping address when billingAddressId is null")
    void shouldDefaultBillingAddressToShipping() {
        CheckoutRequest request = new CheckoutRequest(1L, null, null);

        stubStandardCheckout();
        stubOrderSave();

        orderService.checkout(1L, request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertSame(address, captor.getValue().getShippingAddress());
        assertSame(address, captor.getValue().getBillingAddress());
    }

    @Test
    @DisplayName("Should require a matching hunting license for hunting products")
    void shouldRequireHuntingLicense() {
        setCartLine(BigDecimal.valueOf(100.0), 1, 10);
        requireLicense(LicenseType.HUNTING);
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();

        LicenseRequiredException ex = assertThrows(LicenseRequiredException.class, () -> orderService.checkout(1L, request));
        assertTrue(ex.getMessage().contains("Rod"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should accept checkout when user has the matching hunting license")
    void shouldAcceptCheckoutWithHuntingLicense() {
        setCartLine(BigDecimal.valueOf(100.0), 1, 10);
        requireLicense(LicenseType.HUNTING);
        user.setHuntingLicenseNumber("HT-1234");
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();
        stubOrderSave();

        OrderResponse response = orderService.checkout(1L, request);
        assertNotNull(response.orderNumber());
    }

    @Test
    @DisplayName("Should require fishing license for fishing products")
    void shouldRequireFishingLicense() {
        requireLicense(LicenseType.FISHING);
        user.setHuntingLicenseNumber("HT-1234");
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();

        assertThrows(LicenseRequiredException.class, () -> orderService.checkout(1L, request));
    }

    @Test
    @DisplayName("Should require either license when product licenseType is null")
    void shouldRequireEitherLicenseForNullLicenseType() {
        requireLicense(null);
        CheckoutRequest request = new CheckoutRequest(1L, 1L, null);

        stubStandardCheckout();

        assertThrows(LicenseRequiredException.class, () -> orderService.checkout(1L, request));

        user.setFishingLicenseNumber("FS-999");
        // re-stub checkout now that the user has a license (no save yet happened)
        stubOrderSave();
        OrderResponse response = orderService.checkout(1L, request);
        assertNotNull(response.orderNumber());
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

        stubStandardCheckout();
        when(couponService.validateAndGet("SAVE10", 1L, BigDecimal.valueOf(200.0))).thenReturn(coupon);
        when(couponService.calculateDiscount(any(), any())).thenReturn(BigDecimal.valueOf(20.0));
        stubOrderSave();

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

    @Test
    @DisplayName("Should transition PENDING to PAID")
    void shouldTransitionPendingToPaid() {
        Order order = orderWithId(OrderStatus.PENDING);
        stubOrderSave();

        orderService.transitionStatus(order, OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("Should reject invalid transition PENDING to SHIPPED")
    void shouldRejectInvalidTransition() {
        Order order = orderWithId(OrderStatus.PENDING);

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.transitionStatus(order, OrderStatus.SHIPPED));
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("Should reject self transition")
    void shouldRejectSelfTransition() {
        Order order = orderWithId(OrderStatus.PAID);

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.transitionStatus(order, OrderStatus.PAID));
    }

    @Test
    @DisplayName("Should reject transition from terminal states")
    void shouldRejectTerminalTransitions() {
        for (OrderStatus terminal : List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED)) {
            Order order = orderWithId(terminal);
            assertThrows(InvalidOrderStatusTransitionException.class,
                    () -> orderService.transitionStatus(order, OrderStatus.PAID));
        }
    }

    @Test
    @DisplayName("Should reject transition of an unsaved order")
    void shouldRejectTransientOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.transitionStatus(order, OrderStatus.PAID));
    }

    @Test
    @DisplayName("Should record RETURN stock movements when order is cancelled")
    void shouldRecordReturnMovementsOnCancel() {
        Order order = orderWithId(OrderStatus.PENDING);
        order.setOrderNumber("ORD-1");

        OrderItem item = new OrderItem();
        ProductItem productItem = new ProductItem();
        productItem.setId(1L);
        item.setProductItem(productItem);
        item.setQuantity(2);
        order.getItems().add(item);

        stubOrderSave();

        Order updated = orderService.transitionStatus(order, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
        verify(stockMovementService).recordMovement(argThat(request ->
                request.productItemId().equals(1L)
                        && request.quantity() == 2
                        && request.type() == MovementType.RETURN));
    }

    @Test
    @DisplayName("Should cancel only from PENDING or PAID")
    void shouldCancelOnlyFromPendingOrPaid() {
        Order shipped = orderWithId(OrderStatus.SHIPPED);
        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.transitionStatus(shipped, OrderStatus.CANCELLED));

        Order pending = orderWithId(OrderStatus.PENDING);
        stubOrderSave();
        orderService.transitionStatus(pending, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, pending.getStatus());
    }

    private void requireLicense(LicenseType licenseType) {
        Product product = ((CartItem) cart.getItems().getFirst()).getProductItem().getProduct();
        product.setRequiresLicense(true);
        product.setLicenseType(licenseType);
    }

    private Order orderWithId(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(status);
        order.setUser(user);
        return order;
    }
}