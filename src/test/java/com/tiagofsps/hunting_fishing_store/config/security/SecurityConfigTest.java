package com.tiagofsps.hunting_fishing_store.config.security;

import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks of the security rules defined in SecurityConfig and the
 * controller-level @PreAuthorize annotations.
 */
class SecurityConfigTest extends BaseIntegrationTest {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Brand savedBrand;
    private Category savedCategory;

    @BeforeEach
    void setup() {
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
    void anonymousCanReadPublicCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/promotions"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotReadCoupons() throws Exception {
        mockMvc.perform(get("/api/v1/coupons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousCannotWriteProducts() throws Exception {
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotWriteProducts() throws Exception {
        // A valid body so the request reaches method security instead of being
        // rejected earlier by bean validation (which would answer 400).
        String request = """
                {
                    "name": "Forbidden Rod",
                    "slug": "forbidden-rod",
                    "description": "Should never be created",
                    "requiresLicense": false,
                    "brandId": 1,
                    "categoryId": 1,
                    "items": []
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateProduct() throws Exception {
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

        mockMvc.perform(post("/api/v1/products")
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void regularUserCannotReadCoupons() throws Exception {
        mockMvc.perform(get("/api/v1/coupons").with(userJwt()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/coupons/999").with(userJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotUpdatePaymentStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/payments/999/status")
                        .with(userJwt())
                        .param("status", "COMPLETED"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotUpdatePaymentStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/payments/999/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousCanSubscribeNewsletter() throws Exception {
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "anonymous@example.com"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousCanReachPaymentWebhook() throws Exception {
        // 404 (unknown payment) proves the request passed the public matcher
        // and reached controller logic; without the matcher it would be 401.
        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Webhook-Signature", "local-webhook-secret-change-me")
                        .content("""
                                {
                                    "paymentId": 999999,
                                    "outcome": "SUCCESS"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void anonymousCanReadProductsAsPaginatedPage() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("sort", "name")
                        .param("direction", "asc")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousRejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "500"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/products").param("sort", "price"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousCanReachActuatorHealthButNotMetrics() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotReadActuatorMetrics() throws Exception {
        mockMvc.perform(get("/actuator/metrics").with(userJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCanReachOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCanRequestAndSubmitPasswordReset() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "nobody@example.com"
                                }
                                """))
                .andExpect(status().isAccepted());

        // 400 (unknown token) proves reset-password is reachable anonymously.
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "token": "bogus-token",
                                    "newPassword": "new-secret-password"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousCannotListUsersOrOrders() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotListUsersOrOrders() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(userJwt()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/orders").with(userJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListPaginatedUsersAndOrders() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(adminJwt()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/orders").with(adminJwt()))
                .andExpect(status().isOk());
    }
}