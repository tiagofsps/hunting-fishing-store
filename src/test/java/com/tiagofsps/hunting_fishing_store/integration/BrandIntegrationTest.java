package com.tiagofsps.hunting_fishing_store.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class BrandIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrandRepository brandRepository;

    @BeforeEach
    void cleanDatabase() {
        brandRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void shouldReturnAllBrands() throws Exception {

        Brand shimano = new Brand();
        shimano.setName("Shimano");
        shimano.setSlug("shimano");
        shimano.setLogoUrl("shimano.png");

        Brand daiwa = new Brand();
        daiwa.setName("Daiwa");
        daiwa.setSlug("daiwa");
        daiwa.setLogoUrl("daiwa.png");

        brandRepository.save(shimano);
        brandRepository.save(daiwa);

        mockMvc.perform(
                get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Shimano"))
                .andExpect(jsonPath("$[0].slug").value("shimano"))
                .andExpect(jsonPath("$[1].name").value("Daiwa"))
                .andExpect(jsonPath("$[1].slug").value("daiwa"));
    }

    @Test
    @WithMockUser
    void shouldReturnBrandById() throws Exception {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        Brand saved = brandRepository.save(brand);

        mockMvc.perform(
                get("/api/v1/brands/{id}", saved.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Shimano"))
                .andExpect(jsonPath("$.slug").value("shimano"))
                .andExpect(jsonPath("$.logoUrl").value("logo.png"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenBrandDoesNotExist() throws Exception {

        mockMvc.perform(
                get("/api/v1/brands/{id}", 999999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void shouldCreateBrand() throws Exception {

        String request = """
                {
                    "name": "Shimano",
                    "slug": "shimano",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Shimano"))
                .andExpect(jsonPath("$.slug").value("shimano"))
                .andExpect(jsonPath("$.logoUrl").value("logo.png"));
    }

    @Test
    @WithMockUser
    void shouldReturnConflictWhenCreatingBrandWithExistingSlug() throws Exception {

        Brand existingBrand = new Brand();
        existingBrand.setName("Shimano");
        existingBrand.setSlug("shimano");
        existingBrand.setLogoUrl("shimano.png");

        brandRepository.save(existingBrand);

        String request = """
                {
                    "name": "Another Shimano",
                    "slug": "shimano",
                    "logoUrl": "another.png"
                }
                """;

        mockMvc.perform(
                post("/api/v1/brands")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenCreatingInvalidBrand() throws Exception {

        String request = """
                {
                    "name": "",
                    "slug": "",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                post("/api/v1/brands")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldUpdateBrand() throws Exception {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("old-logo.png");

        Brand saved = brandRepository.save(brand);

        String request = """
                {
                    "name": "Shimano Updated",
                    "slug": "shimano-updated",
                    "logoUrl": "new-logo.png"
                }
                """;

        mockMvc.perform(
                put("/api/v1/brands/{id}", saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(saved.getId()))
        .andExpect(jsonPath("$.name").value("Shimano Updated"))
        .andExpect(jsonPath("$.slug").value("shimano-updated"))
        .andExpect(jsonPath("$.logoUrl").value("new-logo.png"));

        Brand updated = brandRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals("Shimano Updated", updated.getName());
        assertEquals("shimano-updated", updated.getSlug());
        assertEquals("new-logo.png", updated.getLogoUrl());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenUpdatingNonExistingBrand() throws Exception {

        String request = """
                {
                    "name": "Shimano",
                    "slug": "shimano",
                    "logoUrl": "logo.png"
                }
                """;

        mockMvc.perform(
                put("/api/v1/brands/{id}", 999999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldReturnDuplicatedSlugWhenUpdatingBrand() throws Exception {

        Brand shimano = new Brand();
        shimano.setName("Shimano");
        shimano.setSlug("shimano");
        shimano.setLogoUrl("shimano.png");

        Brand daiwa = new Brand();
        daiwa.setName("Daiwa");
        daiwa.setSlug("daiwa");
        daiwa.setLogoUrl("daiwa.png");

        brandRepository.save(shimano);
        Brand savedDaiwa = brandRepository.save(daiwa);

        String request = """
                {
                    "name": "Daiwa",
                    "slug": "shimano",
                    "logoUrl": "logo.png"   
                }
                """;

        mockMvc.perform(
                put("/api/v1/brands/{id}", savedDaiwa.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void shouldDeleteBrand() throws Exception {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        Brand saved = brandRepository.save(brand);

        mockMvc.perform(
                delete("/api/v1/brands/{id}", saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        
        )
        .andExpect(status().isNoContent());

        assertTrue(brandRepository.findById(saved.getId()).isEmpty());

    }

    @Test
    @WithMockUser
    void shouldDeleteNonExistentBrand() throws Exception {

        mockMvc.perform(
                delete("/api/v1/brands/{id}", 999999L)
                        .with(csrf())
        )
        .andExpect(status().isNotFound());

    }

}