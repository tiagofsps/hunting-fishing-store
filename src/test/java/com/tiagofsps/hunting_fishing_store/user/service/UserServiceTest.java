package com.tiagofsps.hunting_fishing_store.user.service;

import com.tiagofsps.hunting_fishing_store.common.exception.UserAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
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
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.findByEmail("test@example.com");

        assertEquals("test@example.com", response.email());
    }

    @Test
    @DisplayName("Should throw when email not found")
    void shouldThrowWhenEmailNotFound() {
        when(userRepository.findByEmail("any@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findByEmail("any@example.com"));
    }

    @Test
    @DisplayName("Should add address to user")
    void shouldAddAddress() {
        AddressRequest request = new AddressRequest("John", "123", "Street", "City", "123", "PT", AddressType.SHIPPING);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.addAddress(1L, request);

        assertEquals(1, response.addresses().size());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw when user not found during add address")
    void shouldThrowWhenUserNotFoundAddingAddress() {
        AddressRequest request = new AddressRequest("John", "123", "Street", "City", "123", "PT", AddressType.SHIPPING);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.addAddress(1L, request));
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
    }
}
