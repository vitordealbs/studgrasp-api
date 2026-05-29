package com.studgrasp.api.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassInsightResponseDTO {
    private String classId;
    private List<String> insights;
}
