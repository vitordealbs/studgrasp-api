package com.studgrasp.api.infra.security;

import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "studgrasp-dev-secret-change-in-production");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);

        user = User.builder()
                .name("Vitor Santos")
                .email("vitor@studgrasp.com")
                .passwordHash("hashed")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    @DisplayName("deve gerar token válido para um usuário")
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("deve extrair email corretamente do token")
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateToken(user);
        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("vitor@studgrasp.com");
    }

    @Test
    @DisplayName("deve validar token correto")
    void shouldValidateCorrectToken() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("deve invalidar token de outro usuário")
    void shouldInvalidateTokenFromDifferentUser() {
        String token = jwtService.generateToken(user);

        User otherUser = User.builder()
                .email("outro@studgrasp.com")
                .role(UserRole.STUDENT)
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("deve invalidar token expirado")
    void shouldInvalidateExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }
}