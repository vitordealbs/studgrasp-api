package com.studgrasp.api.domain.progress.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProgressResponseDTO(
        UUID id,
        UUID userId,
        UUID nodeId,
        String status,
        LocalDateTime updatedAt
) {}