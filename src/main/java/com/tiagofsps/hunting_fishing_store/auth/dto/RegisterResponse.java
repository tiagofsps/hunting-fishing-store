package com.tiagofsps.hunting_fishing_store.auth.dto;

import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;

public record RegisterResponse(UserResponse user, String accessToken, String tokenType) {
}
