package com.tiagofsps.hunting_fishing_store.common.exception;

public class AddressInUseException extends RuntimeException {

    public AddressInUseException(Long addressId) {
        super("Address cannot be deleted because it is referenced by an order: " + addressId);
    }
}