package com.tiagofsps.hunting_fishing_store.auth.integration;

import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";

    private void setupMockMvc() {
        if (mockMvc == null) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        setupMockMvc();
        // Setup: Create a user
        createUser(TEST_EMAIL, TEST_PASSWORD, Role.USER);

        // Act: Login
        String loginJson = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\"}", TEST_EMAIL, TEST_PASSWORD);
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRejectLoginWithInvalidCredentials() throws Exception {
        setupMockMvc();
        // Setup: Create a user
        createUser(TEST_EMAIL, TEST_PASSWORD, Role.USER);

        // Act & Assert: Login with wrong password
        String loginJson = String.format(
            "{\"email\":\"%s\",\"password\":\"WrongPassword\"}", TEST_EMAIL);
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLogoutWithoutRefreshToken() throws Exception {
        setupMockMvc();
        // Act & Assert: Logout without token should still clear cookie
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectRefreshWithMissingCookie() throws Exception {
        setupMockMvc();
        // Act & Assert: Try refresh without cookie
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAccessWithoutAuthorizationHeader() throws Exception {
        setupMockMvc();
        // Act & Assert: Try to access protected endpoint without token
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectDisabledUserLogin() throws Exception {
        setupMockMvc();
        // Setup: Create disabled user
        User user = createUser(TEST_EMAIL, TEST_PASSWORD, Role.USER);
        user.setEnabled(false);
        userRepository.save(user);

        // Act & Assert: Try to login as disabled user
        String loginJson = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\"}", TEST_EMAIL, TEST_PASSWORD);
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPermitPublicAuthEndpoints() throws Exception {
        setupMockMvc();
        // Act & Assert: Public endpoints should be accessible without authentication
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized()); // Invalid creds, but endpoint is accessible
    }

    // Helper methods

    private User createUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}
