package com.tiagofsps.hunting_fishing_store.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tiagofsps.hunting_fishing_store.BaseControllerTest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.CategoryCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.CategoryResponse;
import com.tiagofsps.hunting_fishing_store.catalog.service.CategoryService;
import com.tiagofsps.hunting_fishing_store.common.dto.LocalizedTextResponse;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;

class CategoryControllerTest extends BaseControllerTest {

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldReturnAllCategories() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing", "fishing", "Desc", Map.of("en", new LocalizedTextResponse("Fishing", "Desc"), "pt", new LocalizedTextResponse("Pesca", "Desc")), null, 0L);
        when(categoryService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fishing"))
                .andExpect(jsonPath("$[0].translations.pt.name").value("Pesca"))
                .andExpect(jsonPath("$[0].slug").value("fishing"));

        verify(categoryService).findAll();
    }

    @Test
    void shouldReturnCategoryById() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing", "fishing", "Desc", Map.of("en", new LocalizedTextResponse("Fishing", "Desc"), "pt", new LocalizedTextResponse("Pesca", "Desc")), null, 0L);
        when(categoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/1").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fishing"))
                .andExpect(jsonPath("$.translations.pt.name").value("Pesca"));

        verify(categoryService).findById(1L);
    }

    @Test
    void shouldCreateCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing", "fishing", "Desc", Map.of("en", new LocalizedTextResponse("Fishing", "Desc"), "pt", new LocalizedTextResponse("Pesca", "Desc")), null, 0L);
        when(categoryService.create(any(CategoryCreateRequest.class))).thenReturn(response);

        String request = """
                {
                    "name": "Fishing",
                    "translations": {
                        "en": {"name": "Fishing", "description": "Desc"},
                        "pt": {"name": "Pesca", "description": "Desc"}
                    },
                    "slug": "fishing",
                    "description": "Desc"
                }
                """;

        mockMvc.perform(post("/api/v1/categories").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fishing"))
                .andExpect(jsonPath("$.translations.pt.name").value("Pesca"));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing Updated", "fishing-updated", "New Desc", Map.of("en", new LocalizedTextResponse("Fishing Updated", "New Desc"), "pt", new LocalizedTextResponse("Pesca Atualizada", "New Desc")), null, 0L);
        when(categoryService.update(any(Long.class), any(CategoryCreateRequest.class))).thenReturn(response);

        String request = """
                {
                    "name": "Fishing Updated",
                    "translations": {
                        "en": {"name": "Fishing Updated", "description": "New Desc"},
                        "pt": {"name": "Pesca Atualizada", "description": "New Desc"}
                    },
                    "slug": "fishing-updated",
                    "description": "New Desc"
                }
                """;

        mockMvc.perform(put("/api/v1/categories/1").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fishing Updated"))
                .andExpect(jsonPath("$.translations.pt.name").value("Pesca Atualizada"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1").with(adminJwt()))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(categoryService.findById(1L)).thenThrow(new CategoryNotFoundException(1L));

        mockMvc.perform(get("/api/v1/categories/1").with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenCategoryAlreadyExists() throws Exception {
        when(categoryService.create(any())).thenThrow(new CategoryAlreadyExistsException("fishing"));

        String request = """
                {
                    "name": "Fishing",
                    "slug": "fishing"
                }
                """;

        mockMvc.perform(post("/api/v1/categories").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenInputIsInvalid() throws Exception {
        String request = """
                {
                    "name": "",
                    "slug": ""
                }
                """;

        mockMvc.perform(post("/api/v1/categories").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }
}
