package com.studgrasp.api.domain.flashcard.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FlashcardAttemptResponseDTO(
        UUID id,
        UUID userId,
        UUID flashcardId,
        boolean correct,
        LocalDateTime answeredAt,
        LocalDateTime nextReviewAt
) {}