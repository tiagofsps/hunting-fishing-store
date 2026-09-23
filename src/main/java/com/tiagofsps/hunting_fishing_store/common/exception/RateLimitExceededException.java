package com.tiagofsps.hunting_fishing_store.common.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(long windowSeconds) {
        super("Too many attempts. Please try again in " + windowSeconds + " seconds.");
    }
}