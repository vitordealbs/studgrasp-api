package com.studgrasp.api.domain.dashboard.dto;

import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdvisorDashboardResponseDTO {
    private String classId;
    private ClassStatsDTO classStats;
    private List<WeakTopicDTO> weakTopics;
    private List<StudentSummaryDTO> students;
    private List<String> insights;
}
