package com.tiagofsps.hunting_fishing_store.catalog.controller;

import com.tiagofsps.hunting_fishing_store.BaseControllerTest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.StockMovementRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.StockMovementResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.MovementType;
import com.tiagofsps.hunting_fishing_store.catalog.service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StockMovementControllerTest extends BaseControllerTest {

    @MockitoBean
    private StockMovementService stockMovementService;

    @Test
    void shouldRecordMovement() throws Exception {
        StockMovementResponse response = new StockMovementResponse(1L, 1L, "SKU-1", 5, MovementType.PURCHASE, "Note", null, LocalDateTime.now());
        when(stockMovementService.recordMovement(any(StockMovementRequest.class))).thenReturn(response);

        String request = """
                {
                    "productItemId": 1,
                    "quantity": 5,
                    "type": "PURCHASE",
                    "notes": "Note"
                }
                """;

        mockMvc.perform(post("/api/v1/stock-movements").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }
}
