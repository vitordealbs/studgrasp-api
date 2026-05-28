package com.studgrasp.api.domain.session.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record StudySessionRequestDTO(
        @NotNull UUID nodeId,
        @NotNull LocalDateTime startedAt
) {}
