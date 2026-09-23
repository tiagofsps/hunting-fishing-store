package com.tiagofsps.hunting_fishing_store.user.service;

import com.tiagofsps.hunting_fishing_store.common.exception.AddressInUseException;
import com.tiagofsps.hunting_fishing_store.common.exception.AddressNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.WrongCurrentPasswordException;
import com.tiagofsps.hunting_fishing_store.common.mapper.AddressMapper;
import com.tiagofsps.hunting_fishing_store.common.service.MailService;
import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.order.repository.OrderRepository;
import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressUpdateRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.PasswordChangeRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.ProfileUpdateRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.AddressRepository;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
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

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private MailService mailService;

    private UserService userService;

    private User user;

    private Address address;

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository, passwordEncoder, new AddressMapper(),
                addressRepository, orderRepository, authorizationUtil, mailService);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("encoded-password");
        user.setAddresses(new ArrayList<>());

        address = new Address();
        address.setId(10L);
        address.setUser(user);
        user.getAddresses().add(address);
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

        assertEquals(2, response.addresses().size());
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

    @Test
    @DisplayName("Should update an address that belongs to the user")
    void shouldUpdateAddress() {
        AddressUpdateRequest request = new AddressUpdateRequest("Jane", "999", "New St", "Lisbon", "1000", "PT", AddressType.SHIPPING);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.updateAddress(1L, 10L, request);

        assertEquals("Jane", address.getRecipientName());
        assertEquals("New St", address.getStreet());
        verify(addressRepository).save(address);
    }

    @Test
    @DisplayName("Should throw when updating an address of another user")
    void shouldThrowWhenUpdatingForeignAddress() {
        AddressUpdateRequest request = new AddressUpdateRequest("Jane", "999", "New St", "Lisbon", "1000", "PT", AddressType.SHIPPING);
        User other = new User();
        other.setId(2L);
        address.setUser(other);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        assertThrows(AddressNotFoundException.class, () -> userService.updateAddress(1L, 10L, request));
        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete an address not referenced by any order")
    void shouldDeleteAddress() {
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(orderRepository.existsByShippingAddressIdOrBillingAddressId(10L, 10L)).thenReturn(false);

        userService.deleteAddress(1L, 10L);

        verify(addressRepository).delete(address);
    }

    @Test
    @DisplayName("Should throw 409 when deleting an address referenced by an order")
    void shouldThrowWhenDeletingAddressInUse() {
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(orderRepository.existsByShippingAddressIdOrBillingAddressId(10L, 10L)).thenReturn(true);

        assertThrows(AddressInUseException.class, () -> userService.deleteAddress(1L, 10L));
        verify(addressRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should apply a partial profile update")
    void shouldApplyPartialProfileUpdate() {
        ProfileUpdateRequest request = new ProfileUpdateRequest("NewFirst", null, "999888777", null, "FS-42");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateProfile(1L, request);

        assertEquals("NewFirst", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("999888777", response.phone());
        assertNull(response.huntingLicenseNumber());
        assertEquals("FS-42", response.fishingLicenseNumber());
    }

    @Test
    @DisplayName("Should change password when current password matches")
    void shouldChangePassword() {
        PasswordChangeRequest request = new PasswordChangeRequest("old-password", "new-password-123");
        when(authorizationUtil.getAuthenticatedUser()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password-123")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(1L, request);

        assertEquals("new-encoded", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw 400 when current password is wrong")
    void shouldThrowWhenCurrentPasswordWrong() {
        PasswordChangeRequest request = new PasswordChangeRequest("wrong-password", "new-password-123");
        when(authorizationUtil.getAuthenticatedUser()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(WrongCurrentPasswordException.class, () -> userService.changePassword(1L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deny password change when an admin acts on another user")
    void shouldDenyAdminChangingOtherUserPassword() {
        User admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@example.com");
        PasswordChangeRequest request = new PasswordChangeRequest("admin-pass", "new-password-123");
        when(authorizationUtil.getAuthenticatedUser()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        assertThrows(AccessDeniedException.class, () -> userService.changePassword(1L, request));
        verify(userRepository, never()).save(any());
    }
}