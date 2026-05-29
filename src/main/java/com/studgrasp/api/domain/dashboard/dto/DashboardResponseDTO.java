package com.studgrasp.api.domain.dashboard.dto;

import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponseDTO {
    private String userId;
    private UserStatsDTO stats;
    private List<WeakTopicDTO> weakTopics;
    private List<String> insights;
}
