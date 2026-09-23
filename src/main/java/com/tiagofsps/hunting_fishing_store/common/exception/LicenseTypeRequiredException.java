package com.tiagofsps.hunting_fishing_store.common.exception;

public class LicenseTypeRequiredException extends RuntimeException {

    public LicenseTypeRequiredException() {
        super("licenseType is required when requiresLicense is true");
    }
}
