package com.studgrasp.api.domain.group.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequestDTO(
        @NotBlank String content
) {}
