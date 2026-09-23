package com.tiagofsps.hunting_fishing_store.auth.controller;

import com.tiagofsps.hunting_fishing_store.auth.service.TokenService;
import com.tiagofsps.hunting_fishing_store.config.security.CookieProvider;
import com.tiagofsps.hunting_fishing_store.user.dto.request.UserRegistrationRequest;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.BaseControllerTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseControllerTest {

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CookieProvider cookieProvider;

    @Test
    void shouldCreateAccount() throws Exception {
        UserResponse response = new UserResponse(1L, "test@example.com", "John", "Doe", null, null, null, true, List.of(), Role.USER);
        when(userService.register(any(UserRegistrationRequest.class))).thenReturn(response);
        when(tokenService.generateAccessToken(eq("test@example.com"), anyString())).thenReturn("access-token");
        when(tokenService.generateRefreshToken("test@example.com")).thenReturn("refresh-token");
        when(cookieProvider.createRefreshTokenCookie("refresh-token")).thenReturn(
                ResponseCookie.from("refresh_token", "refresh-token").build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@example.com","password":"password123","firstName":"John","lastName":"Doe"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldRejectAnonymousMeRequest() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String email = "test@example.com";
        String password = "password123";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenService.generateAccessToken(eq(email), anyString())).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(email)).thenReturn(refreshToken);
        when(cookieProvider.createRefreshTokenCookie(refreshToken)).thenReturn(
                ResponseCookie.from("refresh_token", refreshToken).build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void shouldRefreshSuccessfully() throws Exception {
        String email = "test@example.com";
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String tokenId = "token-123";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(email);
        when(jwt.getId()).thenReturn(tokenId);
        when(jwt.getClaimAsString("token_type")).thenReturn("refresh");

        when(jwtDecoder.decode(oldRefreshToken)).thenReturn(jwt);
        when(tokenService.validateRefreshToken(tokenId)).thenReturn(true);

        UserDetails userDetails = new User(email, "", true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        when(tokenService.generateAccessToken(eq(email), anyString())).thenReturn(newAccessToken);
        when(tokenService.generateRefreshToken(email)).thenReturn(newRefreshToken);
        when(cookieProvider.createRefreshTokenCookie(newRefreshToken)).thenReturn(
                ResponseCookie.from("refresh_token", newRefreshToken).build());

        mockMvc.perform(post("/api/v1/auth/refresh").with(userJwt())
                        .cookie(new Cookie("refresh_token", oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(header().exists("Set-Cookie"));

        verify(tokenService).revokeRefreshToken(tokenId);
    }

    @Test
    void shouldFailRefreshIfCookieMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailRefreshIfTokenRevoked() throws Exception {
        String oldRefreshToken = "revoked-token";
        String tokenId = "revoked-id";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn(tokenId);
        when(jwt.getClaimAsString("token_type")).thenReturn("refresh");

        when(jwtDecoder.decode(oldRefreshToken)).thenReturn(jwt);
        when(tokenService.validateRefreshToken(tokenId)).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/refresh").with(userJwt())
                        .cookie(new Cookie("refresh_token", oldRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        String refreshToken = "some-token";
        String tokenId = "token-id";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn(tokenId);
        when(jwt.getClaimAsString("token_type")).thenReturn("refresh");

        when(jwtDecoder.decode(refreshToken)).thenReturn(jwt);
        when(cookieProvider.createRefreshTokenClearCookie()).thenReturn(
                ResponseCookie.from("refresh_token", "").maxAge(0).build());

        mockMvc.perform(post("/api/v1/auth/logout").with(userJwt())
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));

        verify(tokenService).revokeRefreshToken(tokenId);
    }

    @Test
    void shouldLogoutEvenIfTokenInvalid() throws Exception {
        String invalidToken = "invalid-token";

        when(jwtDecoder.decode(invalidToken)).thenThrow(new BadCredentialsException("Invalid"));
        when(cookieProvider.createRefreshTokenClearCookie()).thenReturn(
                ResponseCookie.from("refresh_token", "").maxAge(0).build());

        mockMvc.perform(post("/api/v1/auth/logout").with(userJwt())
                        .cookie(new Cookie("refresh_token", invalidToken)))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }
}
