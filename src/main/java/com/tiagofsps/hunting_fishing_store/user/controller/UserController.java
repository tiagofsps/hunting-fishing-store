package com.tiagofsps.hunting_fishing_store.user.controller;

import com.tiagofsps.hunting_fishing_store.user.dto.request.AddressRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.register(request);
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("/{id}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return userService.addAddress(id, request);
    }
}
