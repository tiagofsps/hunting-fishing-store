package com.tiagofsps.hunting_fishing_store.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tiagofsps.hunting_fishing_store.BaseControllerTest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.ProductCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.ProductResponse;
import com.tiagofsps.hunting_fishing_store.catalog.service.ProductService;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidSortFieldException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductNotFoundException;

class ProductControllerTest extends BaseControllerTest {

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldReturnAllProducts() throws Exception {
        ProductResponse response = new ProductResponse(1L, "Fishing Rod", "fishing-rod", "Desc", false, null, 1L, 1L, List.of(), List.of());
        when(productService.findAll(null, null, null, "createdAt", "desc", 0, 20))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/products").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Fishing Rod"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).findAll(null, null, null, "createdAt", "desc", 0, 20);
    }

    @Test
    void shouldReturnInvalidSortFieldWhenSortNotWhitelisted() throws Exception {
        when(productService.findAll(null, null, null, "description", "asc", 0, 20))
                .thenThrow(new InvalidSortFieldException("description"));

        mockMvc.perform(get("/api/v1/products").param("sort", "description")
                        .with(adminJwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "500")
                        .with(adminJwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnProductById() throws Exception {
        ProductResponse response = new ProductResponse(1L, "Fishing Rod", "fishing-rod", "Desc", false, null, 1L, 1L, List.of(), List.of());
        when(productService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fishing Rod"));

        verify(productService).findById(1L);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductResponse response = new ProductResponse(1L, "Fishing Rod", "fishing-rod", "Desc", false, null, 1L, 1L, List.of(), List.of());
        when(productService.create(any(ProductCreateRequest.class))).thenReturn(response);

        String request = """
                {
                    "name": "Fishing Rod",
                    "slug": "fishing-rod",
                    "categoryId": 1,
                    "brandId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/products").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fishing Rod"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        ProductResponse response = new ProductResponse(1L, "Fishing Rod Updated", "fishing-rod-updated", "New Desc", false, null, 1L, 1L, List.of(), List.of());
        when(productService.update(any(Long.class), any(ProductCreateRequest.class))).thenReturn(response);

        String request = """
                {
                    "name": "Fishing Rod Updated",
                    "slug": "fishing-rod-updated",
                    "categoryId": 1,
                    "brandId": 1
                }
                """;

        mockMvc.perform(put("/api/v1/products/1").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fishing Rod Updated"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1").with(adminJwt()))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.findById(1L)).thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/api/v1/products/1").with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenProductAlreadyExists() throws Exception {
        when(productService.create(any())).thenThrow(new ProductAlreadyExistsException("rod"));

        String request = """
                {
                    "name": "Rod",
                    "slug": "rod",
                    "categoryId": 1,
                    "brandId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/products").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }
}
