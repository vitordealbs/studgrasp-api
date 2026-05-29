package com.studgrasp.api.domain.dashboard.dto;

import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClassDashboardResponseDTO {
    private String classId;
    private String className;
    private int totalStudents;
    private Double avgRetentionRate;
    private int totalReviewedToday;
    private List<WeakTopicDTO> weakTopics;
    private List<StudentStatsDTO> students;
    private List<String> insights;
}
