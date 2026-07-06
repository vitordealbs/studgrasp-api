package com.studgrasp.api.domain.auth;

import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRepository;
import com.studgrasp.api.domain.user.UserRole;
import com.studgrasp.api.infra.security.JwtService;
import com.studgrasp.api.infra.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role() != null && request.role().equalsIgnoreCase("ADVISOR")
                        ? UserRole.ADVISOR
                        : UserRole.STUDENT)
                .acceptedTermsAt(java.time.LocalDateTime.now())
                .build();

        userRepository.save(user);

        return new AuthResponse(
                jwtService.generateToken(user),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email()).orElseThrow();

        return new AuthResponse(
                jwtService.generateToken(user),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public void logout(String token) {
        long remainingMillis = jwtService.getRemainingValidityMillis(token);
        tokenBlacklistService.blacklist(token, remainingMillis);
    }
}
