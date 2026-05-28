package com.studgrasp.api.domain.flashcard.dto;

import jakarta.validation.constraints.NotNull;

public record FlashcardAttemptRequestDTO(
        @NotNull boolean correct
) {}
