package com.studgrasp.api.domain.roadmap.dto;

import java.util.UUID;

public record RoadmapNodeResponseDTO(
        UUID id,
        UUID parentId,
        String title,
        String description,
        Integer nodeOrder,
        String nodeType
) {}