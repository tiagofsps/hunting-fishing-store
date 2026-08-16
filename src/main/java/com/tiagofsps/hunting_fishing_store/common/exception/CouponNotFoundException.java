package com.tiagofsps.hunting_fishing_store.common.exception;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String code) {
        super("Coupon with code '%s' was not found.".formatted(code));
    }

    public CouponNotFoundException(Long id) {
        super("Coupon with id '%d' was not found.".formatted(id));
    }
}
