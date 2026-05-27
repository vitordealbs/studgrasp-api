package com.studgrasp.api.domain.flashcard.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FlashcardResponseDTO(
        UUID id,
        UUID nodeId,
        String question,
        String answer,
        String difficulty,
        boolean aiGenerated,
        LocalDateTime createdAt
) {}