package com.studgrasp.api.domain.progress.dto;

import java.util.UUID;

public record RoadmapProgressSummaryDTO(
        UUID roadmapId,
        int totalNodes,
        int completedNodes
) {}
