package com.studgrasp.api.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassStatsDTO {
    private int totalStudents;
    private Double avgRetentionRate;
    private int totalReviewedToday;
}
