package com.tiagofsps.hunting_fishing_store.promotion.controller;

import com.tiagofsps.hunting_fishing_store.BaseControllerTest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.request.PromotionRequest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.response.PromotionResponse;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.promotion.service.PromotionService;
import com.tiagofsps.hunting_fishing_store.common.exception.PromotionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PromotionControllerTest extends BaseControllerTest {

    @MockitoBean
    private PromotionService promotionService;

    @Test
    void shouldReturnAllPromotions() throws Exception {
        PromotionResponse r1 = new PromotionResponse(1L, "S1", DiscountType.PERCENTAGE, BigDecimal.TEN, 0, true, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, List.of());
        PromotionResponse r2 = new PromotionResponse(2L, "S2", DiscountType.FIXED_AMOUNT, BigDecimal.TEN, 0, true, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, List.of());
        when(promotionService.findAll()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/v1/promotions").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(promotionService).findAll();
    }

    @Test
    void shouldCreatePromotion() throws Exception {
        PromotionResponse response = new PromotionResponse(1L, "Sale", DiscountType.PERCENTAGE, BigDecimal.valueOf(10), 0, true, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, List.of());
        when(promotionService.create(any(PromotionRequest.class))).thenReturn(response);

        String request = """
                {
                    "name": "Sale",
                    "discountType": "PERCENTAGE",
                    "discountValue": 10,
                    "validFrom": "2026-08-15T00:00:00",
                    "validTo": "2026-08-20T00:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/promotions").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sale"));
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldsMissing() throws Exception {
        String request = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/v1/promotions").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }

    @Test
    void shouldReturnNotFoundWhenPromotionDoesNotExist() throws Exception {
        when(promotionService.findById(1L)).thenThrow(new PromotionNotFoundException(1L));

        mockMvc.perform(get("/api/v1/promotions/1").with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePromotion() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/1").with(adminJwt()))
                .andExpect(status().isNoContent());

        verify(promotionService).delete(1L);
    }
}
