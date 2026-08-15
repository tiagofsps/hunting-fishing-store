package com.tiagofsps.hunting_fishing_store.user.service;

import com.tiagofsps.hunting_fishing_store.common.exception.UserAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAddresses(new ArrayList<>());
    }

    @Test
    @DisplayName("Should register a new user")
    void shouldRegisterUser() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "test@example.com", "password123", "John", "Doe", null, null, null
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.register(request);

        assertEquals("test@example.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowWhenEmailExists() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "test@example.com", "password123", "John", "Doe", null, null, null
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Should find user by id")
    void shouldFindUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
    }
}
