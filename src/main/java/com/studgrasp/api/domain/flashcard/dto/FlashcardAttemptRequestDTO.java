package com.studgrasp.api.domain.flashcard.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FlashcardAttemptRequestDTO(
        @NotNull UUID userId,
        @NotNull boolean correct
) {}