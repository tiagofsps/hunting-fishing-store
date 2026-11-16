package com.tiagofsps.hunting_fishing_store.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;

class CategoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateCategory() throws Exception {
        String request = """
                {
                    "name": "Fishing",
                    "slug": "fishing",
                    "description": "Fishing gear",
                    "translations": {
                        "en": {"name": "Fishing", "description": "Fishing gear"},
                        "pt": {"name": "Pesca", "description": "Equipamento de pesca"}
                    }
                }
                """;

        mockMvc.perform(
                post("/api/v1/categories")
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fishing"))
                .andExpect(jsonPath("$.translations.pt.name").value("Pesca"))
                .andExpect(jsonPath("$.slug").value("fishing"));
    }

    @Test
    void shouldCreateSubcategory() throws Exception {
        Category parent = new Category();
        parent.setName("Fishing");
        parent.setSlug("fishing");
        Category savedParent = categoryRepository.save(parent);

        String request = """
                {
                    "name": "Rods",
                    "slug": "rods",
                    "translations": {
                        "en": {"name": "Rods"},
                        "pt": {"name": "Caniços"}
                    },
                    "parentCategoryId": %d
                }
                """.formatted(savedParent.getId());

        mockMvc.perform(
                post("/api/v1/categories")
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Rods"))
                .andExpect(jsonPath("$.translations.pt.name").value("Caniços"))
                .andExpect(jsonPath("$.parentCategoryId").value(savedParent.getId()));
    }
}
