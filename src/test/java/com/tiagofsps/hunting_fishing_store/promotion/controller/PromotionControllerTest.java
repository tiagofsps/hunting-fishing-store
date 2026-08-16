package com.tiagofsps.hunting_fishing_store.promotion.controller;

import com.tiagofsps.hunting_fishing_store.promotion.dto.request.PromotionRequest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.response.PromotionResponse;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.promotion.service.PromotionService;
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

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionService promotionService;

    @Test
    void shouldReturnAllPromotions() throws Exception {
        PromotionResponse response = new PromotionResponse(1L, "Sale", DiscountType.PERCENTAGE, BigDecimal.valueOf(10), 0, true, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true, List.of());
        when(promotionService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sale"));

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

        mockMvc.perform(post("/api/v1/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sale"));
    }
}
