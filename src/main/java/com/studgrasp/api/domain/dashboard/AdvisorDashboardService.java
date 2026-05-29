package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.dashboard.dto.*;
import com.studgrasp.api.domain.flashcard.FlashcardAttemptRepository;
import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdvisorDashboardService {

    private final FlashcardAttemptRepository repo;
    private final AiInsightsClient aiClient;

    @Transactional(readOnly = true)
    public AdvisorDashboardResponseDTO getDashboard(UUID classId) {
        List<StudentSummaryDTO> students = repo.findStudentStatsByClassId(classId)
                .stream()
                .map(row -> StudentSummaryDTO.builder()
                        .userId(UUID.fromString((String) row[0]))
                        .userName((String) row[1])
                        .reviewedToday(row[2] != null ? ((Number) row[2]).intValue() : 0)
                        .retentionRate(row[3] != null ? ((Number) row[3]).doubleValue() : null)
                        .build())
                .toList();

        List<WeakTopicDTO> weakTopics = repo.findWeakTopicsByClassId(classId)
                .stream()
                .map(row -> {
                    UUID nodeId = UUID.fromString((String) row[0]);
                    String nodeTitle = (String) row[1];
                    long total = ((Number) row[2]).longValue();
                    long wrong = ((Number) row[3]).longValue();
                    double errorRate = total > 0 ? (double) wrong / total : 0.0;
                    return new WeakTopicDTO(nodeId, nodeTitle, errorRate);
                })
                .toList();

        int totalStudents = students.size();
        int totalReviewedToday = students.stream().mapToInt(StudentSummaryDTO::getReviewedToday).sum();
        OptionalDouble avg = students.stream()
                .filter(s -> s.getRetentionRate() != null)
                .mapToDouble(StudentSummaryDTO::getRetentionRate)
                .average();
        Double avgRetentionRate = avg.isPresent() ? avg.getAsDouble() : null;

        ClassStatsDTO classStats = ClassStatsDTO.builder()
                .totalStudents(totalStudents)
                .avgRetentionRate(avgRetentionRate)
                .totalReviewedToday(totalReviewedToday)
                .build();

        ClassInsightRequestDTO req = ClassInsightRequestDTO.builder()
                .classId(classId.toString())
                .totalStudents(totalStudents)
                .avgRetentionRate(avgRetentionRate)
                .totalReviewedToday(totalReviewedToday)
                .weakTopics(weakTopics)
                .students(students)
                .build();
        List<String> insights = aiClient.fetchClassInsights(req).getInsights();

        return AdvisorDashboardResponseDTO.builder()
                .classId(classId.toString())
                .classStats(classStats)
                .weakTopics(weakTopics)
                .students(students)
                .insights(insights)
                .build();
    }
}
