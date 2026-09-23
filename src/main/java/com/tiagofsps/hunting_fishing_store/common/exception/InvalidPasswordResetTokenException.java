package com.tiagofsps.hunting_fishing_store.common.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super("Password reset token is invalid or has expired");
    }
}