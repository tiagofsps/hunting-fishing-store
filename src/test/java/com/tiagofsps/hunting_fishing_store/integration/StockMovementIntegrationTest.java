package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.catalog.entity.*;
import com.tiagofsps.hunting_fishing_store.catalog.repository.*;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StockMovementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private ProductItem savedItem;

    @BeforeEach
    void setup() {
        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brandRepository.save(brand);

        Category category = new Category();
        category.setName("Fishing");
        category.setSlug("fishing");
        categoryRepository.save(category);

        Product product = new Product();
        product.setName("Rod");
        product.setSlug("rod");
        product.setBrand(brand);
        product.setCategory(category);
        productRepository.save(product);

        ProductItem item = new ProductItem();
        item.setSku("SKU-1");
        item.setPrice(BigDecimal.valueOf(100.0));
        item.setStockQuantity(10);
        item.setProduct(product);
        savedItem = productItemRepository.save(item);
    }

    @Test
    void shouldRecordMovementAndAdjustStock() throws Exception {
        String request = """
                {
                    "productItemId": %d,
                    "quantity": 5,
                    "type": "PURCHASE",
                    "notes": "Restock"
                }
                """.formatted(savedItem.getId());

        mockMvc.perform(post("/api/v1/stock-movements")
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.type").value("PURCHASE"));

        ProductItem updatedItem = productItemRepository.findById(savedItem.getId()).orElseThrow();
        assertEquals(15, updatedItem.getStockQuantity());
    }

    @Test
    void shouldReturnBadRequestWhenInsufficientStock() throws Exception {
        String request = """
                {
                    "productItemId": %d,
                    "quantity": -20,
                    "type": "SALE",
                    "notes": "Large order"
                }
                """.formatted(savedItem.getId());

        mockMvc.perform(post("/api/v1/stock-movements")
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        ProductItem item = productItemRepository.findById(savedItem.getId()).orElseThrow();
        assertEquals(10, item.getStockQuantity()); // Stock should remain unchanged
    }

    @Test
    void shouldGetHistoryForItem() throws Exception {
        StockMovement movement = new StockMovement();
        movement.setProductItem(savedItem);
        movement.setQuantity(2);
        movement.setType(MovementType.PURCHASE);
        stockMovementRepository.save(movement);

        mockMvc.perform(get("/api/v1/stock-movements/item/" + savedItem.getId())
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }
}
