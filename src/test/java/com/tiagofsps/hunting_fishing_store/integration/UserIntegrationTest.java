package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void shouldRegisterUser() throws Exception {
        String request = """
                {
                    "email": "test@example.com",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void shouldAddAddressToUser() throws Exception {
        String registrationRequest = """
                {
                    "email": "test@example.com",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andReturn().getResponse().getContentAsString();
        
        Long userId = Long.valueOf(registerResponse.split(",")[0].split(":")[1]);

        String addressRequest = """
                {
                    "recipientName": "John Doe",
                    "recipientPhone": "123456789",
                    "street": "Main St",
                    "city": "City",
                    "postalCode": "12345",
                    "countryCode": "PT",
                    "addressType": "SHIPPING"
                }
                """;

        mockMvc.perform(post("/api/v1/users/" + userId + "/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addresses[0].street").value("Main St"));
    }
}
