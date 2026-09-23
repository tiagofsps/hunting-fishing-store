package com.tiagofsps.hunting_fishing_store.auth.controller;

import com.tiagofsps.hunting_fishing_store.auth.dto.request.ForgotPasswordRequest;
import com.tiagofsps.hunting_fishing_store.auth.dto.request.ResetPasswordRequest;
import com.tiagofsps.hunting_fishing_store.auth.service.PasswordResetService;
import com.tiagofsps.hunting_fishing_store.config.security.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, registration, tokens and password reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final RateLimitService rateLimitService;

    public PasswordResetController(PasswordResetService passwordResetService, RateLimitService rateLimitService) {
        this.passwordResetService = passwordResetService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Request a password reset email (always 202, no account enumeration)")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        rateLimitService.check("forgot-password:email:" + request.email());
        passwordResetService.requestReset(request.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Set a new password using a one-time reset token")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }
}