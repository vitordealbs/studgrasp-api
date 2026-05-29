package com.studgrasp.api.domain.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponseDTO {
    private String userId;
    private List<String> insights;
}
