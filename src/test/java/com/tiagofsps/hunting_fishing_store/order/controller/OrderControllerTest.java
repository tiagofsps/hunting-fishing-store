package com.tiagofsps.hunting_fishing_store.order.controller;

import com.tiagofsps.hunting_fishing_store.order.dto.request.CheckoutRequest;
import com.tiagofsps.hunting_fishing_store.order.dto.response.OrderResponse;
import com.tiagofsps.hunting_fishing_store.order.entity.OrderStatus;
import com.tiagofsps.hunting_fishing_store.order.service.OrderService;
import com.tiagofsps.hunting_fishing_store.common.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCheckout() throws Exception {
        OrderResponse response = new OrderResponse(1L, "ORD-123", LocalDateTime.now(), OrderStatus.PENDING, BigDecimal.valueOf(200.0), BigDecimal.ZERO, 1L, List.of());
        when(orderService.checkout(any(Long.class), any(CheckoutRequest.class))).thenReturn(response);

        String request = """
                {
                    "shippingAddressId": 1,
                    "billingAddressId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/orders/checkout/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-123"));

        verify(orderService).checkout(any(Long.class), any(CheckoutRequest.class));
    }

    @Test
    void shouldReturnOrdersByUser() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOrderByNumber() throws Exception {
        OrderResponse response = new OrderResponse(1L, "ORD-123", LocalDateTime.now(), OrderStatus.PENDING, BigDecimal.valueOf(200.0), BigDecimal.ZERO, 1L, List.of());
        when(orderService.getOrderByNumber("ORD-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/ORD-123"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        when(orderService.getOrderByNumber("XXX")).thenThrow(new OrderNotFoundException("XXX"));

        mockMvc.perform(get("/api/v1/orders/XXX"))
                .andExpect(status().isNotFound());
    }
}
