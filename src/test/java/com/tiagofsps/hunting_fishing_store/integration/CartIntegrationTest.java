package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductItemRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    private User savedUser;
    private ProductItem savedItem;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setFirstName("John");
        user.setLastName("Doe");
        savedUser = userRepository.save(user);

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
    void shouldAddItemToCartAndRetrieveIt() throws Exception {
        String addRequest = """
                {
                    "productItemId": %d,
                    "quantity": 2
                }
                """.formatted(savedItem.getId());

        mockMvc.perform(post("/api/v1/carts/" + savedUser.getId() + "/items")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(200.0));

        mockMvc.perform(get("/api/v1/carts/" + savedUser.getId())
                        .with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
    }
}
