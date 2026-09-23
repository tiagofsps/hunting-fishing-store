package com.tiagofsps.hunting_fishing_store.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 8)
        String newPassword
) {
}