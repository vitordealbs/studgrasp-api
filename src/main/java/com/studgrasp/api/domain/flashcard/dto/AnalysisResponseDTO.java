package com.studgrasp.api.domain.flashcard.dto;

import java.util.List;
import java.util.UUID;

public record AnalysisResponseDTO(
        UUID userId,
        List<WeakTopicDTO> weakTopics
) {}
