package com.studgrasp.api.domain.auth;

import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRepository;
import com.studgrasp.api.domain.user.UserRole;
import com.studgrasp.api.infra.security.JwtService;
import com.studgrasp.api.infra.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterNewUser() {
        var request = new RegisterRequest("Vitor", "vitor@test.com", "123456", "STUDENT", true);

        when(userRepository.existsByEmail("vitor@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("token-mock");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("token-mock");
        assertThat(response.email()).isEqualTo("vitor@test.com");
        assertThat(response.role()).isEqualTo("STUDENT");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        var request = new RegisterRequest("Vitor", "vitor@test.com", "123456", "STUDENT", true);

        when(userRepository.existsByEmail("vitor@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        var request = new LoginRequest("vitor@test.com", "123456");
        var user = User.builder()
                .name("Vitor")
                .email("vitor@test.com")
                .passwordHash("hashed")
                .role(UserRole.STUDENT)
                .build();

        when(userRepository.findByEmail("vitor@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token-mock");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token-mock");
        assertThat(response.email()).isEqualTo("vitor@test.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowOnInvalidCredentials() {
        var request = new LoginRequest("vitor@test.com", "wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void logout_shouldDelegateToBlacklistService() {
        String token = "header.payload.signature";
        when(jwtService.getRemainingValidityMillis(token)).thenReturn(30_000L);

        authService.logout(token);

        verify(jwtService).getRemainingValidityMillis(token);
        verify(tokenBlacklistService).blacklist(token, 30_000L);
    }

    @Test
    void logout_shouldBlacklistWithZeroTtlWhenTokenAlreadyExpired() {
        String token = "header.payload.expired";
        when(jwtService.getRemainingValidityMillis(token)).thenReturn(0L);

        authService.logout(token);

        verify(tokenBlacklistService).blacklist(token, 0L);
    }
}
