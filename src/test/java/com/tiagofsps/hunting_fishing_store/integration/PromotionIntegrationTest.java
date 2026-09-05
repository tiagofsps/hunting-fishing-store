package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.promotion.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PromotionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PromotionRepository promotionRepository;

    @Test
    void shouldCreatePromotionWithRules() throws Exception {
        String request = """
                {
                    "name": "Shimano Special",
                    "discountType": "PERCENTAGE",
                    "discountValue": 20.0,
                    "validFrom": "2026-08-01T00:00:00",
                    "validTo": "2026-08-30T00:00:00",
                    "rules": [
                        {
                            "targetType": "BRAND",
                            "targetId": 1
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/promotions")
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Shimano Special"))
                .andExpect(jsonPath("$.rules.length()").value(1));
    }
}
