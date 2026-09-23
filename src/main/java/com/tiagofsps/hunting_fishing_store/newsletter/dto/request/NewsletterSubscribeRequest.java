package com.tiagofsps.hunting_fishing_store.newsletter.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewsletterSubscribeRequest(
        @NotBlank
        @Email
        String email
) {
}