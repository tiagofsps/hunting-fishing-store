package com.tiagofsps.hunting_fishing_store.auth.integration;

import com.jayway.jsonpath.JsonPath;
import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.auth.service.TokenService;
import com.tiagofsps.hunting_fishing_store.config.security.JwtAuthenticationConverter;
import com.tiagofsps.hunting_fishing_store.user.entity.Role;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Guards the whole authorization pipeline behind {@code @PreAuthorize("hasRole('ADMIN')")}:
 * TokenService writes the {@code roles} claim as a JSON array, NimbusJwtDecoder
 * hands it back as a {@link List}, and JwtAuthenticationConverter must turn that
 * into {@code ROLE_ADMIN}. Every stage is exercised with real beans so a claim
 * shape mismatch surfaces here instead of as a 403 "Access Denied" in the app.
 */
@Transactional
class AdminAuthorizationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    @Test
    void adminAccessTokenShouldDecodeToAdminAuthority() {
        String token = tokenService.generateAccessToken("admin-roundtrip@example.com", "ROLE_ADMIN");

        Jwt decoded = jwtDecoder.decode(token);

        Object decodedRolesClaim = decoded.getClaim("roles");
        assertThat(decodedRolesClaim).isInstanceOf(List.class);
        assertThat(converter.convert(decoded).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void adminShouldCallAdminEndpointWithLoginToken() throws Exception {
        String email = "admin-endpoint@example.com";
        createUser(email, "password123", Role.ADMIN);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(loginResponse, "$.accessToken");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void regularUserShouldBeForbiddenFromAdminEndpoint() throws Exception {
        String token = tokenService.generateAccessToken("regular-endpoint@example.com", "ROLE_USER");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName("Admin");
        user.setLastName("Tester");
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.saveAndFlush(user);
    }
}
