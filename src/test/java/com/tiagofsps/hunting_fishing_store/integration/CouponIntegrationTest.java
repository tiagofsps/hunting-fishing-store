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
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.coupon.repository.CouponRepository;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.AddressRepository;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CouponIntegrationTest extends BaseIntegrationTest {

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
    private CouponRepository couponRepository;

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
        cartItem.setQuantity(1);
        cart.getItems().add(cartItem);
        cartRepository.save(cart);

        Coupon coupon = new Coupon();
        coupon.setCode("SAVE10");
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(10.0));
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        couponRepository.save(coupon);
    }

    @Test
    void shouldApplyCouponDuringCheckout() throws Exception {
        String checkoutRequest = """
                {
                    "shippingAddressId": %d,
                    "billingAddressId": %d,
                    "couponCode": "SAVE10"
                }
                """.formatted(savedAddress.getId(), savedAddress.getId());

        mockMvc.perform(post("/api/v1/orders/checkout/" + savedUser.getId())
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(90.0));
    }
}
