package com.studgrasp.api.domain.roadmap.dto;

import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeResponseDTO;

import java.util.List;
import java.util.UUID;

public record RoadmapResponseDTO(
        UUID id,
        String title,
        String careerType,
        String sourceUrl,
        List<RoadmapNodeResponseDTO> nodes
) {}