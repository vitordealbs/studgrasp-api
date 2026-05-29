package com.studgrasp.api.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StudentSummaryDTO {
    private UUID userId;
    private String userName;
    private int reviewedToday;
    private Double retentionRate;
}
