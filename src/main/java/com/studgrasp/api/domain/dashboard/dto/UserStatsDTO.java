package com.studgrasp.api.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserStatsDTO {
    private int streak;
    private int reviewedToday;
    private int correctToday;
    private int dueNow;
    private String nextReviewAt;
    private int totalMastered;
    private Double retentionRate;
}
