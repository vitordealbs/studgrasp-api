package com.studgrasp.api.domain.flashcard.dto;

import java.util.UUID;

public record WeakTopicDTO(
        UUID nodeId,
        String nodeTitle,
        double errorRate
) {}
