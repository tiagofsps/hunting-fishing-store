package com.tiagofsps.hunting_fishing_store.user.dto.response;

import com.tiagofsps.hunting_fishing_store.user.entity.AddressType;

public record AddressResponse(
        Long id,
        String recipientName,
        String recipientPhone,
        String street,
        String city,
        String postalCode,
        String countryCode,
        AddressType addressType
) {
}
