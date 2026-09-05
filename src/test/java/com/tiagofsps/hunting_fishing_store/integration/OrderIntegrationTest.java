package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import com.tiagofsps.hunting_fishing_store.cart.repository.CartRepository;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductItemRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.AddressRepository;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

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

    @Autowired
    private OrderRepository orderRepository;

    private User savedUser;
    private Address savedAddress;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setFirstName("John");
        user.setLastName("Doe");
        savedUser = userRepository.save(user);

        Address address = new Address();
        address.setRecipientName("John Doe");
        address.setRecipientPhone("123456789");
        address.setStreet("Main St");
        address.setCity("City");
        address.setPostalCode("12345");
        address.setCountryCode("PT");
        address.setAddressType(AddressType.SHIPPING);
        address.setUser(savedUser);
        savedAddress = addressRepository.save(address);
        
        savedUser.setAddresses(List.of(savedAddress));

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
        ProductItem savedItem = productItemRepository.save(item);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProductItem(savedItem);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);
        cartRepository.save(cart);
    }

    @Test
    @Transactional
    void shouldCheckoutSuccessfully() throws Exception {
        String checkoutRequest = """
                {
                    "shippingAddressId": %d,
                    "billingAddressId": %d
                }
                """.formatted(savedAddress.getId(), savedAddress.getId());

        mockMvc.perform(post("/api/v1/orders/checkout/" + savedUser.getId())
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(200.0))
                .andExpect(jsonPath("$.items.length()").value(1));

        Cart cart = cartRepository.findByUserId(savedUser.getId()).orElseThrow();
        assertTrue(cart.getItems().isEmpty());
        assertEquals(1, orderRepository.count());
    }
}
