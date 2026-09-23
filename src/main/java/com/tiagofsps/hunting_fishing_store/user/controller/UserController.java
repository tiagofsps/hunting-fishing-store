package com.tiagofsps.hunting_fishing_store.user.controller;

import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressUpdateRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.PasswordChangeRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.ProfileUpdateRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profiles and addresses")
public class UserController {

    private final UserService userService;
    private final AuthorizationUtil authorizationUtil;

    public UserController(UserService userService, AuthorizationUtil authorizationUtil) {
        this.userService = userService;
        this.authorizationUtil = authorizationUtil;
    }

    private void authorize(Long targetUserId) {
        if (!authorizationUtil.isAuthorizedToAccess(targetUserId, null)) {
            throw new AccessDeniedException("You do not have permission to modify this user's profile");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Paginated user list for the dashboard")
    public Page<UserResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user profile (owner or ADMIN)")
    public UserResponse findById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);

        // Check authorization: user can only access their own profile or admins can access any
        if (!authorizationUtil.isAuthorizedToAccess(id, user.email())) {
            throw new AccessDeniedException("You do not have permission to access this user's profile");
        }

        return user;
    }

    @PostMapping("/{id}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an address to a user (owner or ADMIN)")
    public UserResponse addAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        authorize(id);
        return userService.addAddress(id, request);
    }

    @PutMapping("/{id}/addresses/{addressId}")
    @Operation(summary = "Update an address (owner or ADMIN)")
    public UserResponse updateAddress(@PathVariable Long id, @PathVariable Long addressId,
                                      @Valid @RequestBody AddressUpdateRequest request) {
        authorize(id);
        return userService.updateAddress(id, addressId, request);
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an address (owner or ADMIN)")
    public void deleteAddress(@PathVariable Long id, @PathVariable Long addressId) {
        authorize(id);
        userService.deleteAddress(id, addressId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Partially update a profile (owner or ADMIN)")
    public UserResponse updateProfile(@PathVariable Long id, @Valid @RequestBody ProfileUpdateRequest request) {
        authorize(id);
        return userService.updateProfile(id, request);
    }

    @PostMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change your own password")
    public void changePassword(@PathVariable Long id, @Valid @RequestBody PasswordChangeRequest request) {
        // Owner-only: enforced inside the service so even admins cannot change
        // another user's password.
        userService.changePassword(id, request);
    }
}