package com.tiagofsps.hunting_fishing_store.auth.controller;

import com.tiagofsps.hunting_fishing_store.auth.dto.LoginRequest;
import com.tiagofsps.hunting_fishing_store.auth.dto.LoginResponse;
import com.tiagofsps.hunting_fishing_store.auth.dto.RegisterResponse;
import com.tiagofsps.hunting_fishing_store.auth.service.TokenService;
import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.config.security.CookieProvider;
import com.tiagofsps.hunting_fishing_store.config.security.RateLimitService;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @Mock
    private CookieProvider cookieProvider;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @InjectMocks
    private AuthController authController;

    private UserResponse user(Long id, String email) {
        return new UserResponse(id, email, "John", "Doe", null, "HT-1", "FS-1", true, List.of(), Role.USER);
    }

    @Test
    void meReturnsAuthenticatedUserProfile() {
        UserResponse expected = user(7L, "test@example.com");
        when(authorizationUtil.getAuthenticatedUser()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(expected);

        UserResponse result = authController.me();

        assertSame(expected, result);
    }

    @Test
    void loginPopulatesUserIdAndRole() {
        UserResponse user = user(5L, "test@example.com");
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(
                "test@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(tokenService.generateAccessToken(eq("test@example.com"), eq("ROLE_USER"))).thenReturn("access-token");
        when(tokenService.generateRefreshToken("test@example.com")).thenReturn("refresh-token");
        when(cookieProvider.createRefreshTokenCookie("refresh-token"))
                .thenReturn(ResponseCookie.from("refresh_token", "refresh-token").build());

        HttpServletResponse response = mock(HttpServletResponse.class);
        LoginResponse result = authController.login(new LoginRequest("test@example.com", "pass"), response);

        assertEquals("access-token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(5L, result.userId());
        assertEquals("USER", result.role());
        verify(response).addHeader(eq("Set-Cookie"), any(String.class));
    }

    @Test
    void refreshPopulatesUserIdAndRole() {
        UserResponse user = user(5L, "test@example.com");
        String email = "test@example.com";
        String tokenId = "token-123";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(email);
        when(jwt.getId()).thenReturn(tokenId);
        when(jwt.getClaimAsString("token_type")).thenReturn("refresh");
        when(jwtDecoder.decode("old-refresh-token")).thenReturn(jwt);
        when(tokenService.validateRefreshToken(tokenId)).thenReturn(true);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, "", true, true, true, true, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userService.findByEmail(email)).thenReturn(user);
        when(tokenService.generateAccessToken(eq(email), eq("ROLE_USER"))).thenReturn("new-access-token");
        when(tokenService.generateRefreshToken(email)).thenReturn("new-refresh-token");
        when(cookieProvider.createRefreshTokenCookie("new-refresh-token"))
                .thenReturn(ResponseCookie.from("refresh_token", "new-refresh-token").build());

        HttpServletResponse response = mock(HttpServletResponse.class);
        LoginResponse result = authController.refresh("old-refresh-token", response);

        assertEquals("new-access-token", result.accessToken());
        assertEquals(5L, result.userId());
        assertEquals("USER", result.role());
        verify(tokenService).revokeRefreshToken(tokenId);
    }

    @Test
    void registerReturnsUserWithTokensAndSetsCookie() {
        UserResponse created = user(9L, "new@example.com");
        when(userService.register(any(UserRegistrationRequest.class))).thenReturn(created);
        when(tokenService.generateAccessToken(eq("new@example.com"), eq("ROLE_USER"))).thenReturn("access-token");
        when(tokenService.generateRefreshToken("new@example.com")).thenReturn("refresh-token");
        when(cookieProvider.createRefreshTokenCookie("refresh-token"))
                .thenReturn(ResponseCookie.from("refresh_token", "refresh-token").build());

        HttpServletResponse response = mock(HttpServletResponse.class);
        UserRegistrationRequest request = new UserRegistrationRequest(
                "new@example.com", "password123", "John", "Doe", null, null, null);

        RegisterResponse result = authController.register(request, response);

        assertSame(created, result.user());
        assertEquals("access-token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        verify(response).addHeader(eq("Set-Cookie"), any(String.class));
    }
}