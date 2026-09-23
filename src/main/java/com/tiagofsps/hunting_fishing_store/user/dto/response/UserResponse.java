package com.tiagofsps.hunting_fishing_store.user.dto.response;

import com.tiagofsps.hunting_fishing_store.user.entity.Role;

import java.util.List;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String huntingLicenseNumber,
        String fishingLicenseNumber,
        Boolean enabled,
        List<AddressResponse> addresses,
        Role role
) {
}
