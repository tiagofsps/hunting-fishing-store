package com.tiagofsps.hunting_fishing_store.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Brand savedBrand;
    private Category savedCategory;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        savedBrand = brandRepository.save(brand);

        Category category = new Category();
        category.setName("Fishing");
        category.setSlug("fishing");
        savedCategory = categoryRepository.save(category);
    }

    @Test
    @WithMockUser
    void shouldCreateProductWithItemsAndImages() throws Exception {
        String request = """
                {
                    "name": "Fishing Rod",
                    "slug": "fishing-rod",
                    "description": "High quality rod",
                    "requiresLicense": false,
                    "brandId": %d,
                    "categoryId": %d,
                    "items": [
                        {
                            "sku": "ROD-001",
                            "price": 150.00,
                            "stockQuantity": 5,
                            "weight": 0.3,
                            "attributes": {
                                "length": "2.4m",
                                "material": "Carbon"
                            }
                        }
                    ],
                    "images": [
                        {
                            "imageUrl": "rod.png",
                            "altText": "Fishing Rod",
                            "isPrimary": true,
                            "displayOrder": 1
                        }
                    ]
                }
                """.formatted(savedBrand.getId(), savedCategory.getId());

        mockMvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fishing Rod"))
                .andExpect(jsonPath("$.items[0].sku").value("ROD-001"))
                .andExpect(jsonPath("$.items[0].attributes.length").value("2.4m"))
                .andExpect(jsonPath("$.images[0].imageUrl").value("rod.png"));
        
        assertEquals(1, productRepository.count());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenBrandDoesNotExist() throws Exception {
        String request = """
                {
                    "name": "Fishing Rod",
                    "slug": "fishing-rod",
                    "brandId": 9999,
                    "categoryId": %d
                }
                """.formatted(savedCategory.getId());

        mockMvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }
}
