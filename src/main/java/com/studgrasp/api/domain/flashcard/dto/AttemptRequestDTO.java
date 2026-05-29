package com.studgrasp.api.domain.flashcard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AttemptRequestDTO(
        @NotNull UUID flashcardId,
        @NotNull @Min(0) @Max(5) int quality
) {}
