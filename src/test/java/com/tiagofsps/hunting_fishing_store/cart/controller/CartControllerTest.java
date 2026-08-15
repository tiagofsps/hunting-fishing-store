package com.tiagofsps.hunting_fishing_store.cart.controller;

import com.tiagofsps.hunting_fishing_store.cart.dto.request.AddToCartRequest;
import com.tiagofsps.hunting_fishing_store.cart.dto.response.CartResponse;
import com.tiagofsps.hunting_fishing_store.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void shouldReturnCart() throws Exception {
        CartResponse response = new CartResponse(1L, 1L, List.of(), BigDecimal.ZERO);
        when(cartService.getCart(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(cartService).getCart(1L);
    }

    @Test
    void shouldAddItemToCart() throws Exception {
        CartResponse response = new CartResponse(1L, 1L, List.of(), BigDecimal.ZERO);
        when(cartService.addToCart(any(Long.class), any(AddToCartRequest.class))).thenReturn(response);

        String request = """
                {
                    "productItemId": 1,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/v1/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }
}
