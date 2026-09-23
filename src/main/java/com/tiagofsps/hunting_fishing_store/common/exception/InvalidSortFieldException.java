package com.tiagofsps.hunting_fishing_store.common.exception;

import java.util.List;

public class InvalidSortFieldException extends RuntimeException {

    public InvalidSortFieldException(String field, List<String> allowed) {
        super("Invalid sort parameter '" + field + "'. Allowed values: " + String.join(", ", allowed));
    }

    public InvalidSortFieldException(String field) {
        super("Invalid sort parameter '" + field + "'");
    }
}