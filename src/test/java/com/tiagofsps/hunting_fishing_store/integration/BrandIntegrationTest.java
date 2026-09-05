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
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;

class BrandIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BrandRepository brandRepository;

    @Test
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
                get("/api/v1/brands").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Shimano"))
                .andExpect(jsonPath("$[0].slug").value("shimano"))
                .andExpect(jsonPath("$[1].name").value("Daiwa"))
                .andExpect(jsonPath("$[1].slug").value("daiwa"));
    }

    @Test
    void shouldReturnBrandById() throws Exception {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        Brand saved = brandRepository.save(brand);

        mockMvc.perform(
                get("/api/v1/brands/{id}", saved.getId())
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Shimano"))
                .andExpect(jsonPath("$.slug").value("shimano"))
                .andExpect(jsonPath("$.logoUrl").value("logo.png"));
    }

    @Test
    void shouldReturnNotFoundWhenBrandDoesNotExist() throws Exception {

        mockMvc.perform(
                get("/api/v1/brands/{id}", 999999L)
                        .with(adminJwt()))
                .andExpect(status().isNotFound());
    }

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
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Shimano"))
                .andExpect(jsonPath("$.slug").value("shimano"))
                .andExpect(jsonPath("$.logoUrl").value("logo.png"));
    }

    @Test
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
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isConflict());
    }

    @Test
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
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
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
                        .with(adminJwt())
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
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isNotFound());
    }

    @Test
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
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteBrand() throws Exception {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        Brand saved = brandRepository.save(brand);

        mockMvc.perform(
                delete("/api/v1/brands/{id}", saved.getId())
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)        
        )
        .andExpect(status().isNoContent());

        assertTrue(brandRepository.findById(saved.getId()).isEmpty());

    }

    @Test
    void shouldDeleteNonExistentBrand() throws Exception {

        mockMvc.perform(
                delete("/api/v1/brands/{id}", 999999L)
                        .with(adminJwt())
        )
        .andExpect(status().isNotFound());

    }

}