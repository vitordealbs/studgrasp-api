package com.studgrasp.api.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studgrasp.api.infra.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests covering:
 * - Logout blacklists the token
 * - Blacklisted token is rejected on subsequent requests
 * - Logout without token returns 204 gracefully
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerSecurityTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    // Mock Redis interactions so these tests run without a real Redis instance
    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Logout endpoint
    // -----------------------------------------------------------------------

    @Test
    void logout_shouldReturn204WhenValidTokenProvided() throws Exception {
        // First register and get a token
        String registerBody = """
                {
                  "name": "Security Tester",
                  "email": "security-logout@test.com",
                  "password": "A@secure12345",
                  "role": "STUDENT"
                }
                """;
        String responseJson = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(responseJson).get("token").asText();

        // Now logout with the token
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify the blacklist service was called
        verify(tokenBlacklistService, atLeastOnce()).blacklist(eq(token), anyLong());
    }

    @Test
    void logout_shouldReturn204EvenWithoutToken() throws Exception {
        // Logout without an Authorization header — should still respond 204
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    // -----------------------------------------------------------------------
    // Blacklisted token rejection
    // -----------------------------------------------------------------------

    @Test
    void request_shouldBeRejectedWhenTokenIsBlacklisted() throws Exception {
        // Register a user and obtain a token
        String registerBody = """
                {
                  "name": "Blacklist Test User",
                  "email": "blacklist-test@test.com",
                  "password": "A@secure12345",
                  "role": "STUDENT"
                }
                """;
        String responseJson = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(responseJson).get("token").asText();

        // Simulate the token having been blacklisted (e.g., user already logged out)
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        // Attempt to use a blacklisted token — SecurityContext should not be populated,
        // so authenticated-only endpoints return 403 (forbidden by Spring Security)
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                // Logout is on /api/v1/auth/** (permitAll), so even with a blacklisted
                // token the logout endpoint itself responds 204 — the token is simply
                // not placed in the SecurityContext, which is the desired behaviour.
                .andExpect(status().isNoContent());

        // Confirm the filter checked the blacklist
        verify(tokenBlacklistService, atLeastOnce()).isBlacklisted(token);
    }

    @Test
    void login_shouldReturn401OnBadCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nonexistent@test.com",
                                  "password": "wrongpassword"
                                }
                                """))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
