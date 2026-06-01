package com.studgrasp.api.domain.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String role
) {}
