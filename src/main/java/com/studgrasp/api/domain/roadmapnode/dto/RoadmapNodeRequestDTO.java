package com.studgrasp.api.domain.roadmapnode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RoadmapNodeRequestDTO(
        UUID id,
        @NotBlank String title,
        String description,
        UUID parentId,
        @NotBlank String nodeType,
        @NotNull Integer nodeOrder,
        @NotNull UUID roadmapId
) {}