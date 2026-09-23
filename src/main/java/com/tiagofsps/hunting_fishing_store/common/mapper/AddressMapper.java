package com.tiagofsps.hunting_fishing_store.common.mapper;

import com.tiagofsps.hunting_fishing_store.user.dto.response.AddressResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getRecipientPhone(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.getAddressType()
        );
    }
}
