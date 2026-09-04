package com.tiagofsps.hunting_fishing_store.auth.controller;

import com.tiagofsps.hunting_fishing_store.auth.dto.LoginRequest;
import com.tiagofsps.hunting_fishing_store.auth.dto.LoginResponse;
import com.tiagofsps.hunting_fishing_store.auth.service.TokenService;
import com.tiagofsps.hunting_fishing_store.config.security.CookieProvider;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final CookieProvider cookieProvider;

    public AuthController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            JwtDecoder jwtDecoder,
            UserDetailsService userDetailsService,
            UserService userService,
            CookieProvider cookieProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.jwtDecoder = jwtDecoder;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.cookieProvider = cookieProvider;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER");

        return tokenResponseWithCookie(authentication.getName(), role, response);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshTokenCookie,
            HttpServletResponse response) {
        
        if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
            throw new BadCredentialsException("Missing refresh token cookie");
        }

        Jwt refreshToken = decodeRefreshToken(refreshTokenCookie);
        String username = refreshToken.getSubject();
        String tokenId = refreshToken.getId();
        
        // Validate token exists in Redis and hasn't been revoked
        if (tokenId == null || !tokenService.validateRefreshToken(tokenId)) {
            throw new BadCredentialsException("Refresh token has been revoked or is invalid");
        }
        
        var userDetails = userDetailsService.loadUserByUsername(username);
        
        // Verify user is enabled
        if (!userDetails.isEnabled()) {
            throw new BadCredentialsException("User account is disabled");
        }
        
        // Revoke the old token to prevent replay
        tokenService.revokeRefreshToken(tokenId);
        
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER");
        
        return tokenResponseWithCookie(username, role, response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(value = "refresh_token", required = false) String refreshTokenCookie,
            HttpServletResponse response) {
        
        // If refresh token exists, decode and revoke it
        if (refreshTokenCookie != null && !refreshTokenCookie.isEmpty()) {
            try {
                Jwt refreshToken = decodeRefreshToken(refreshTokenCookie);
                String tokenId = refreshToken.getId();
                if (tokenId != null) {
                    tokenService.revokeRefreshToken(tokenId);
                }
            } catch (BadCredentialsException e) {
                // Token might be malformed or invalid, but we still clear the cookie
                // Logout should be idempotent
            }
        }
        
        // Always clear the refresh token cookie
        response.addHeader("Set-Cookie", cookieProvider.createRefreshTokenClearCookie().toString());
    }

    private LoginResponse tokenResponseWithCookie(String username, String role, HttpServletResponse response) {
        String accessToken = tokenService.generateAccessToken(username, role);
        String refreshToken = tokenService.generateRefreshToken(username);
        
        // Set refresh token as secure HttpOnly cookie
        response.addHeader("Set-Cookie", cookieProvider.createRefreshTokenCookie(refreshToken).toString());
        
        return new LoginResponse(accessToken, "Bearer");
    }

    private Jwt decodeRefreshToken(String value) {
        try {
            Jwt token = jwtDecoder.decode(value);
            if (!"refresh".equals(token.getClaimAsString("token_type"))) {
                throw new BadCredentialsException("Invalid refresh token");
            }
            return token;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException("Invalid refresh token", exception);
        }
    }
}
