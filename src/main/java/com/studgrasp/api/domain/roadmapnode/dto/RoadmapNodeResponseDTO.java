package com.studgrasp.api.domain.roadmapnode.dto;

import java.util.UUID;

public record RoadmapNodeResponseDTO(
        UUID id,
        String title,
        String description,
        UUID parentId,
        String nodeType,
        Integer nodeOrder,
        UUID roadmapId
) {}