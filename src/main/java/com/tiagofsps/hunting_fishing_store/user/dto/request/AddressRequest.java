package com.tiagofsps.hunting_fishing_store.user.dto.request;

import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank
        String recipientName,

        @NotBlank
        String recipientPhone,

        @NotBlank
        String street,

        @NotBlank
        String city,

        @NotBlank
        String postalCode,

        @NotBlank
        @Size(min = 2, max = 2)
        String countryCode,

        @NotNull
        AddressType addressType
) {
}
