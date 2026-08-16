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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.CategoryCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.CategoryResponse;
import com.tiagofsps.hunting_fishing_store.catalog.service.CategoryService;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldReturnAllCategories() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing", "fishing", "Desc", null);
        when(categoryService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fishing"))
                .andExpect(jsonPath("$[0].slug").value("fishing"));

        verify(categoryService).findAll();
    }

    @Test
    void shouldReturnCategoryById() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing", "fishing", "Desc", null);
        when(categoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fishing"));

        verify(categoryService).findById(1L);
    }

    @Test
    void shouldCreateCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing", "fishing", "Desc", null);
        when(categoryService.create(any(CategoryCreateRequest.class))).thenReturn(response);

        String request = """
                {
                    "name": "Fishing",
                    "slug": "fishing",
                    "description": "Desc"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fishing"));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fishing Updated", "fishing-updated", "New Desc", null);
        when(categoryService.update(any(Long.class), any(CategoryCreateRequest.class))).thenReturn(response);

        String request = """
                {
                    "name": "Fishing Updated",
                    "slug": "fishing-updated",
                    "description": "New Desc"
                }
                """;

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fishing Updated"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(categoryService.findById(1L)).thenThrow(new CategoryNotFoundException(1L));

        mockMvc.perform(get("/api/v1/categories/1"))
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

        mockMvc.perform(post("/api/v1/categories")
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

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }
}
