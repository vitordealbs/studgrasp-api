package com.studgrasp.api.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentStatsDTO {
    private String userId;
    private String userName;
    private int reviewedToday;
    private Double retentionRate;
}
