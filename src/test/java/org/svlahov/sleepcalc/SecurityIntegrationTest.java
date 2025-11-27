package org.svlahov.sleepcalc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.svlahov.sleepcalc.controller.AuthController;
import org.svlahov.sleepcalc.support.TestJwtDynamicProps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static java.util.Objects.requireNonNull;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class SecurityIntegrationTest extends TestJwtDynamicProps {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("POST /api/auth/register schould create a new user")
        void register_shouldCreateUser() throws Exception {
                AuthController.AuthRequest authRequest = new AuthController.AuthRequest();
                authRequest.setUsername("testuser");
                authRequest.setPassword("password");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(requireNonNull(MediaType.APPLICATION_JSON))
                                .content(requireNonNull(objectMapper.writeValueAsString(authRequest))))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/sleep/state should be forbidden for unauthorized users")
        void getSleepState_withoutAuth_shouldBeForbidden() throws Exception {
                mockMvc.perform(get("/api/sleep/state"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("A registered user should be able to log in and access secure endpoints")
        void registeredUser_canLoginAndAccessSecureEndpoints() throws Exception {
                AuthController.AuthRequest userCredentials = new AuthController.AuthRequest();
                userCredentials.setUsername("logintest");
                userCredentials.setPassword("password123");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(requireNonNull(MediaType.APPLICATION_JSON))
                                .content(requireNonNull(objectMapper.writeValueAsString(userCredentials))))
                                .andExpect(status().isOk());

                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(requireNonNull(MediaType.APPLICATION_JSON))
                                .content(requireNonNull(objectMapper.writeValueAsString(userCredentials))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andReturn();

                String responseBody = loginResult.getResponse().getContentAsString();
                String token = objectMapper.readTree(responseBody).get("token").asText();

                mockMvc.perform(get("/api/sleep/state")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk());
        }

}
