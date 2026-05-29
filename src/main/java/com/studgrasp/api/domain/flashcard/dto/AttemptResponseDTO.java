package com.studgrasp.api.domain.flashcard.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttemptResponseDTO(
        UUID id,
        UUID nodeId,
        String question,
        String answer,
        String difficulty,
        LocalDateTime nextReviewAt,
        double easeFactor,
        int intervalDays,
        int repetitions
) {}
