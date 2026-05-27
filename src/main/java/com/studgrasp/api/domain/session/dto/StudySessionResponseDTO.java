package com.studgrasp.api.domain.session.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudySessionResponseDTO(
        UUID id,
        UUID userId,
        UUID nodeId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer durationS
) {}