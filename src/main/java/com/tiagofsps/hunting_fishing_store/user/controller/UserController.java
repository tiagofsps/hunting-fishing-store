package com.tiagofsps.hunting_fishing_store.user.controller;

import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthorizationUtil authorizationUtil;

    public UserController(UserService userService, AuthorizationUtil authorizationUtil) {
        this.userService = userService;
        this.authorizationUtil = authorizationUtil;
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        
        // Check authorization: user can only access their own profile or admins can access any
        if (!authorizationUtil.isAdmin() && !user.email().equals(authorizationUtil.getAuthenticatedUser())) {
            throw new AccessDeniedException("You do not have permission to access this user's profile");
        }
        
        return user;
    }

    @PostMapping("/{id}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        UserResponse user = userService.findById(id);
        
        // Check authorization: user can only add addresses to their own profile or admins can add to any
        if (!authorizationUtil.isAdmin() && !user.email().equals(authorizationUtil.getAuthenticatedUser())) {
            throw new AccessDeniedException("You do not have permission to modify this user's profile");
        }
        
        return userService.addAddress(id, request);
    }
}
