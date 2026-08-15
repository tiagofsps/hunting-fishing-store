package com.tiagofsps.hunting_fishing_store.cart.repository;

import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest
class CartRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveCart() {
        User user = new User();
        user.setEmail("cart@example.com");
        user.setPassword("pass");
        user.setFirstName("C");
        user.setLastName("U");
        userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);

        Cart saved = cartRepository.save(cart);

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindCartByUserId() {
        User user = new User();
        user.setEmail("cart2@example.com");
        user.setPassword("pass");
        user.setFirstName("C");
        user.setLastName("U");
        User savedUser = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);

        Optional<Cart> result = cartRepository.findByUserId(savedUser.getId());

        assertTrue(result.isPresent());
    }
}
