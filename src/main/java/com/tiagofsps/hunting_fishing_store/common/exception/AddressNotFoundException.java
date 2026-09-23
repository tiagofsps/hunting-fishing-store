package com.tiagofsps.hunting_fishing_store.common.exception;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(Long addressId) {
        super("Address not found: " + addressId);
    }
}