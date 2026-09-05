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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tiagofsps.hunting_fishing_store.BaseControllerTest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.BrandCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.BrandResponse;
import com.tiagofsps.hunting_fishing_store.catalog.service.BrandService;
import com.tiagofsps.hunting_fishing_store.common.exception.BrandAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.BrandNotFoundException;


class BrandControllerTest extends BaseControllerTest {

    @MockitoBean
    private BrandService brandService;

    @Test
    void shouldReturnAllBrands() throws Exception {

        BrandResponse response =
            new BrandResponse(
                1L,
                "Shimano",
                "shimano",
                "logo.png");

        when(brandService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/brands").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Shimano"))
                .andExpect(jsonPath("$[0].slug").value("shimano"))
                .andExpect(jsonPath("$[0].logoUrl").value("logo.png"));

        verify(brandService).findAll();
    }

    @Test
    void shouldReturnBrandById() throws Exception {

        BrandResponse response =
                new BrandResponse(
                        1L,
                        "Shimano",
                        "shimano",
                        "logo.png");

        when(brandService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/brands/1").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Shimano"))
                .andExpect(jsonPath("$.slug").value("shimano"))
                .andExpect(jsonPath("$.logoUrl").value("logo.png"));

        verify(brandService).findById(1L);
    }

    @Test
    void shouldCreateBrand() throws Exception {

        BrandResponse response =
                new BrandResponse(
                        1L,
                        "Shimano",
                        "shimano",
                        "logo.png");

        when(brandService.create(any(BrandCreateRequest.class)))
                .thenReturn(response);

        String request = """
                {
                    "name": "Shimano",
                    "slug": "shimano",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                post("/api/v1/brands").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Shimano"))
                .andExpect(jsonPath("$.slug").value("shimano"))
                .andExpect(jsonPath("$.logoUrl").value("logo.png"));

        verify(brandService).create(any(BrandCreateRequest.class));
    }

    @Test
    void shouldRejectInvalidBrand() throws Exception {

        String request = """
                {
                    "name": "",
                    "slug": "",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                post("/api/v1/brands").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(brandService);
    }

    @Test
    void shouldReturnConflictWhenBrandAlreadyExists() throws Exception {

        when(brandService.create(any(BrandCreateRequest.class)))
                .thenThrow(new BrandAlreadyExistsException("shimano"));

        String request = """
                {
                    "name": "Shimano",
                    "slug": "shimano",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                post("/api/v1/brands").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUpdateBrand() throws Exception {

        BrandResponse response =
                new BrandResponse(
                        1L,
                        "New Shimano",
                        "new-shimano",
                        "new-logo.png");

        when(brandService.update(
                any(Long.class),
                any(BrandCreateRequest.class)))
                .thenReturn(response);

        String request = """
                {
                    "name": "New Shimano",
                    "slug": "new-shimano",
                    "logoUrl": "new-logo.png"
                }
                """;

        mockMvc.perform(
                put("/api/v1/brands/1").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Shimano"))
                .andExpect(jsonPath("$.slug").value("new-shimano"))
                .andExpect(jsonPath("$.logoUrl").value("new-logo.png"));

        verify(brandService)
                .update(
                        any(Long.class),
                        any(BrandCreateRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownBrand() throws Exception {

        when(brandService.update(
                any(Long.class),
                any(BrandCreateRequest.class)))
                .thenThrow(new BrandNotFoundException(1L));

        String request = """
                {
                    "name": "Shimano",
                    "slug": "shimano",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                put("/api/v1/brands/1").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenUpdatingDuplicateSlug() throws Exception {

        when(brandService.update(
                any(Long.class),
                any(BrandCreateRequest.class)))
                .thenThrow(new BrandAlreadyExistsException("shimano"));

        String request = """
                {
                    "name": "Shimano",
                    "slug": "shimano",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                put("/api/v1/brands/1").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteBrand() throws Exception {

        mockMvc.perform(delete("/api/v1/brands/1").with(adminJwt()))
                .andExpect(status().isNoContent());

        verify(brandService).delete(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownBrand() throws Exception {

        doThrow(new BrandNotFoundException(1L))
                .when(brandService)
                .delete(1L);

        mockMvc.perform(delete("/api/v1/brands/1").with(adminJwt()))
                .andExpect(status().isNotFound());

        verify(brandService).delete(1L);
    }
}