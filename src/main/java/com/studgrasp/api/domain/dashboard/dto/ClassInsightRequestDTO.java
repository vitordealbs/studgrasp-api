package com.studgrasp.api.domain.dashboard.dto;

import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClassInsightRequestDTO {
    private String classId;
    private int totalStudents;
    private Double avgRetentionRate;
    private int totalReviewedToday;
    private List<WeakTopicDTO> weakTopics;
    private List<StudentStatsDTO> students;
}
