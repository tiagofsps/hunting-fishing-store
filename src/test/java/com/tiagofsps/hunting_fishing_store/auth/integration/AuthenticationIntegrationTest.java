package com.tiagofsps.hunting_fishing_store.auth.integration;

import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_PASSWORD = "SecurePassword123!";

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        String email = "login-success@example.com";
        String rawPassword = "password123";
        createUser(email, rawPassword, Role.USER);

        String loginJson = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\"}", email, rawPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void shouldRejectLoginWithInvalidCredentials() throws Exception {
        String email = "login-fail@example.com";
        createUser(email, TEST_PASSWORD, Role.USER);

        String loginJson = String.format(
            "{\"email\":\"%s\",\"password\":\"WrongPassword\"}", email);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLogoutWithoutRefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void shouldRejectRefreshWithMissingCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAnonymousCatalogAccess() throws Exception {
        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectDisabledUserLogin() throws Exception {
        String email = "disabled@example.com";
        User user = createUser(email, TEST_PASSWORD, Role.USER);
        user.setEnabled(false);
        userRepository.saveAndFlush(user);

        String loginJson = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\"}", email, TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPermitPublicAuthEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    private User createUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.saveAndFlush(user);
    }
}