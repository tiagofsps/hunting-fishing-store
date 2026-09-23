package com.tiagofsps.hunting_fishing_store.auth.dto;

public record LoginResponse(String accessToken, String tokenType, Long userId, String role) {
}
