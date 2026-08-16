package com.tiagofsps.hunting_fishing_store.common.exception;

public class CouponUsageLimitReachedException extends RuntimeException {
    public CouponUsageLimitReachedException(String code) {
        super("Usage limit for coupon '%s' has been reached.".formatted(code));
    }
}
