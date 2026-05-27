package com.studgrasp.api.domain.progress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserProgressRequestDTO(
        @NotNull UUID nodeId,
        @NotBlank String status
) {}