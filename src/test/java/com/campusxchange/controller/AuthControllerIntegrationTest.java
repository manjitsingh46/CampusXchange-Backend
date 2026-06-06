package com.campusxchange.controller;

import com.campusxchange.dto.AuthRequest;
import com.campusxchange.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("integrationtest")
                .email("integration@test.edu")
                .firstName("Integration")
                .lastName("Test")
                .password("IntegrationTest123!")
                .confirmPassword("IntegrationTest123!")
                .college("Test University")
                .phoneNumber("9876543210")
                .studentId("TEST2024001")
                .acceptTerms(true)
                .build();

        authRequest = AuthRequest.builder()
                .email("integration@test.edu")
                .password("IntegrationTest123!")
                .build();
    }

    @Test
    @DisplayName("Should register new user with valid data")
    void testRegisterNewUserSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("integration@test.edu"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Should reject registration with invalid email")
    void testRegisterInvalidEmail() throws Exception {
        registerRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void testRegisterShortPassword() throws Exception {
        registerRequest.setPassword("short");
        registerRequest.setConfirmPassword("short");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should reject login with invalid credentials")
    void testLoginInvalidCredentials() throws Exception {
        authRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Should reject login with non-existent email")
    void testLoginNonExistentUser() throws Exception {
        authRequest.setEmail("nonexistent@test.edu");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Should verify email successfully")
    void testVerifyEmailSuccess() throws Exception {
        // Create a unique email for this test
        String uniqueEmail = "verify-" + System.currentTimeMillis() + "@test.edu";
        RegisterRequest request = RegisterRequest.builder()
                .username("verifytest" + System.currentTimeMillis())
                .email(uniqueEmail)
                .firstName("Verify")
                .lastName("Test")
                .password("VerifyTest123!")
                .confirmPassword("VerifyTest123!")
                .college("Test University")
                .phoneNumber("9876543210")
                .studentId("VERIFY2024001")
                .acceptTerms(true)
                .build();

        // Register the user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then verify email
        mockMvc.perform(post("/api/auth/verify-email")
                .param("email", uniqueEmail)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    @DisplayName("Should handle logout request")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
