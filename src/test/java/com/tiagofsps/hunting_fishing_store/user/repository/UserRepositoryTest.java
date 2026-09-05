package com.tiagofsps.hunting_fishing_store.user.repository;

import com.tiagofsps.hunting_fishing_store.BaseRepositoryTest;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindUserByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void shouldCheckIfEmailExists() {
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("pass");
        user.setFirstName("F");
        user.setLastName("L");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("other@example.com"));
    }
}
