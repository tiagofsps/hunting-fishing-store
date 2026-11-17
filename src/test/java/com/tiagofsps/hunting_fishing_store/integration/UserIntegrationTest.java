package com.tiagofsps.hunting_fishing_store.integration;

import com.tiagofsps.hunting_fishing_store.BaseIntegrationTest;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import org.springframework.transaction.annotation.Transactional;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUser() throws Exception {
        String request = """
                {
                    "email": "register-unique@example.com",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldAddAddressToUser() throws Exception {
        String email = "address-unique@example.com";
        String registrationRequest = String.format("""
                {
                    "email": "%s",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """, email);

        // 1. Perform registration endpoint call
        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // 2. Extract the ID from the response JSON (register returns the user
        //    nested under "user" together with the access token)
        Integer userId = JsonPath.read(registerResponse, "$.user.id");

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
                        .with(userJwt(email))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addresses[0].street").value("Main St"));
    }
}