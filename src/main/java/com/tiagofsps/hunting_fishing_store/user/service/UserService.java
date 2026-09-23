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
import com.tiagofsps.hunting_fishing_store.user.dto.response.AddressResponse;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.AddressRepository;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final AuthorizationUtil authorizationUtil;
    private final MailService mailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AddressMapper addressMapper,
                       AddressRepository addressRepository, OrderRepository orderRepository, AuthorizationUtil authorizationUtil,
                       MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressMapper = addressMapper;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.authorizationUtil = authorizationUtil;
        this.mailService = mailService;
    }

    private UserResponse toUserResponse(User user) {
        List<AddressResponse> addresses = user.getAddresses().stream()
                .map(addressMapper::toResponse)
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
                addresses,
                user.getRole()
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

        // Best-effort welcome mail (MailService swallows failures).
        mailService.send(savedUser.getEmail(), "Welcome to Hunting & Fishing Store!",
                "Hi " + savedUser.getFirstName() + ",\n\n"
                        + "Your account has been created. Happy hunting and fishing!");

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

    /**
     * Admin-only dashboard list. Mapping is reused as-is; note the N+1 on
     * addresses per page (fetching strategy is a known follow-up).
     */
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
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

    @Transactional
    public UserResponse updateAddress(Long userId, Long addressId, AddressUpdateRequest request) {
        Address address = findAddressForUser(userId, addressId);

        address.setRecipientName(request.recipientName());
        address.setRecipientPhone(request.recipientPhone());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setPostalCode(request.postalCode());
        address.setCountryCode(request.countryCode());
        address.setAddressType(request.addressType());

        addressRepository.save(address);
        return toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId)));
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = findAddressForUser(userId, addressId);

        // Orders keep FKs (not copies) to shipping/billing addresses, so a
        // referenced address cannot be deleted.
        if (orderRepository.existsByShippingAddressIdOrBillingAddressId(addressId, addressId)) {
            throw new AddressInUseException(addressId);
        }

        addressRepository.delete(address);
    }

    private Address findAddressForUser(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        if (address.getUser() == null || !address.getUser().getId().equals(userId)) {
            throw new AddressNotFoundException(addressId);
        }
        return address;
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.huntingLicenseNumber() != null) {
            user.setHuntingLicenseNumber(request.huntingLicenseNumber());
        }
        if (request.fishingLicenseNumber() != null) {
            user.setFishingLicenseNumber(request.fishingLicenseNumber());
        }

        return toUserResponse(userRepository.save(user));
    }

    /**
     * Owner-only password change. Deliberately stricter than the admin-override
     * rule: even an administrator can only change their own password, so an
     * admin can never learn another user's credentials.
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User authenticatedUser = authorizationUtil.getAuthenticatedUser() == null
                ? null
                : userRepository.findByEmail(authorizationUtil.getAuthenticatedUser()).orElse(null);

        if (authenticatedUser == null || !authenticatedUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only change your own password");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new WrongCurrentPasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}