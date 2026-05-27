package com.studgrasp.api.domain.auth;

public record AuthResponse(
        String token,
        String name,
        String email,
        String role
) {}