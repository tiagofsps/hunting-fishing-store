package com.tiagofsps.hunting_fishing_store.common.exception;

public class LicenseRequiredException extends RuntimeException {

    public LicenseRequiredException(String productName) {
        super("A valid license is required to purchase '%s'. Add a hunting or fishing license number to your profile."
                .formatted(productName));
    }
}
