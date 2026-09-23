package com.tiagofsps.hunting_fishing_store.user.dto.request;

import jakarta.validation.constraints.Size;

/**
 * PATCH-like partial profile update. Only non-null fields are applied; email
 * is deliberately not updatable (it is the JWT subject).
 */
public record ProfileUpdateRequest(
        @Size(max = 255)
        String firstName,

        @Size(max = 255)
        String lastName,

        @Size(max = 255)
        String phone,

        @Size(max = 255)
        String huntingLicenseNumber,

        @Size(max = 255)
        String fishingLicenseNumber
) {
}