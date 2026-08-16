package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.promotion.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PromotionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @BeforeEach
    void setup() {
        promotionRepository.deleteAll();
    }

    @Test
    @WithMockUser
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Shimano Special"))
                .andExpect(jsonPath("$.rules.length()").value(1));
    }
}
