package com.studgrasp.api.domain.dashboard.dto;

import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InsightRequestDTO {
    private String userId;
    private List<WeakTopicDTO> weakTopics;
    private int streak;
    private int reviewedToday;
    private int correctToday;
    private int dueNow;
    private Double retentionRate;
}
