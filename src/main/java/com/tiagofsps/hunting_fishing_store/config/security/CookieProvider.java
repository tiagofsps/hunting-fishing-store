package com.tiagofsps.hunting_fishing_store.config.security;

import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class CookieProvider {

    @Value("${app.security.cookie.secure:true}")
    private boolean secure;

    @Value("${app.security.cookie.same-site:Strict}")
    private String sameSite;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/v1/auth";
    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 604800; // 7 days

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path(COOKIE_PATH)
                .maxAge(Duration.ofSeconds(REFRESH_TOKEN_MAX_AGE_SECONDS))
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie createRefreshTokenClearCookie() {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .sameSite(sameSite)
                .build();
    }

    public String getRefreshTokenCookieName() {
        return REFRESH_TOKEN_COOKIE_NAME;
    }
}
