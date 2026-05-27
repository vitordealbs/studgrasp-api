package com.studgrasp.api.domain.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MessageRequestDTO(
        @NotBlank String content,
        @NotNull UUID senderId
) {}