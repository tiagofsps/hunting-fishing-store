package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.AbstractContainerBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the per-email rate limit kicks in on the 4th login attempt with a
 * limit of 3. Runs in its own Spring context so only this context is bound to
 * the small limit.
 */
@SpringBootTest(properties = {
        "app.rate-limit.max-attempts=3",
        "app.rate-limit.window-seconds=900"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RateLimitIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String email;

    @BeforeEach
    void clearRateLimitKeys() {
        Set<String> keys = redisTemplate.keys("rate-limit:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        email = "ratelimit-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void shouldReturn429OnFourthLoginAttempt() throws Exception {
        String loginJson = String.format(
                "{\"email\":\"%s\",\"password\":\"WrongPassword\"}", email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isTooManyRequests());
    }
}