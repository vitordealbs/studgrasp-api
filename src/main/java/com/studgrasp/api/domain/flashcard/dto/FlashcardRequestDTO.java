package com.studgrasp.api.domain.flashcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FlashcardRequestDTO(
        @NotNull UUID nodeId,
        @NotBlank String question,
        @NotBlank String answer,
        @NotBlank String difficulty,
        @NotNull boolean aiGenerated
) {}