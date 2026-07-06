package com.studgrasp.api.domain.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoadmapRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 100)
        String title,

        @NotBlank(message = "Career type is required")
        @Size(max = 50)
        String careerType,

        String sourceUrl,

        boolean isPublic
) {}