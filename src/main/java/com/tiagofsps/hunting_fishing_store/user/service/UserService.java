package com.tiagofsps.hunting_fishing_store.user.service;

import com.tiagofsps.hunting_fishing_store.common.exception.UserAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.AddressResponse;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserResponse toUserResponse(User user) {
        List<AddressResponse> addresses = user.getAddresses().stream()
                .map(this::toAddressResponse)
                .toList();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getHuntingLicenseNumber(),
                user.getFishingLicenseNumber(),
                user.getEnabled(),
                addresses
        );
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getRecipientPhone(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.getAddressType()
        );
    }

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setHuntingLicenseNumber(request.huntingLicenseNumber());
        user.setFishingLicenseNumber(request.fishingLicenseNumber());
        user.setRole(Role.USER); // Explicit USER role for all new registrations

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toUserResponse(user);
    }

    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Address address = new Address();
        address.setRecipientName(request.recipientName());
        address.setRecipientPhone(request.recipientPhone());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setPostalCode(request.postalCode());
        address.setCountryCode(request.countryCode());
        address.setAddressType(request.addressType());
        address.setUser(user);

        user.getAddresses().add(address);
        User updatedUser = userRepository.save(user);
        return toUserResponse(updatedUser);
    }
}
