package com.tiagofsps.hunting_fishing_store.common.exception;

public class WrongCurrentPasswordException extends RuntimeException {

    public WrongCurrentPasswordException() {
        super("The current password is incorrect");
    }
}