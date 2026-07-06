package com.studgrasp.api.domain.roadmap.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CoauthorDTO(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        UUID invitedBy,
        LocalDateTime invitedAt
) {}
