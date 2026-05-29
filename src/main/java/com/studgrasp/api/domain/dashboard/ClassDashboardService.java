package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.classroom.ClassroomRepository;
import com.studgrasp.api.domain.dashboard.dto.*;
import com.studgrasp.api.domain.flashcard.FlashcardAttemptRepository;
import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassDashboardService {

    private final FlashcardAttemptRepository attemptRepo;
    private final ClassroomRepository classroomRepo;
    private final AiInsightsClient aiClient;

    @Transactional(readOnly = true)
    public ClassDashboardResponseDTO getDashboard(UUID classId) {
        String className = classroomRepo.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", classId.toString()))
                .getName();

        List<StudentStatsDTO> students = attemptRepo.findStudentStatsByClassId(classId)
                .stream()
                .map(r -> StudentStatsDTO.builder()
                        .userId(r[0].toString())
                        .userName((String) r[1])
                        .reviewedToday(r[2] != null ? ((Number) r[2]).intValue() : 0)
                        .retentionRate(r[3] != null ? ((Number) r[3]).doubleValue() : null)
                        .build())
                .toList();

        List<WeakTopicDTO> weakTopics = attemptRepo.findWeakTopicsByClassId(classId)
                .stream()
                .map(r -> new WeakTopicDTO(
                        UUID.fromString(r[0].toString()),
                        (String) r[1],
                        ((Number) r[2]).doubleValue()))
                .toList();

        int totalStudents = students.size();
        int totalReviewedToday = students.stream().mapToInt(StudentStatsDTO::getReviewedToday).sum();
        OptionalDouble avg = students.stream()
                .filter(s -> s.getRetentionRate() != null)
                .mapToDouble(StudentStatsDTO::getRetentionRate)
                .average();
        Double avgRetentionRate = avg.isPresent() ? avg.getAsDouble() : null;

        ClassInsightRequestDTO req = ClassInsightRequestDTO.builder()
                .classId(classId.toString())
                .totalStudents(totalStudents)
                .avgRetentionRate(avgRetentionRate)
                .totalReviewedToday(totalReviewedToday)
                .weakTopics(weakTopics)
                .students(students)
                .build();
        List<String> insights = aiClient.fetchClassInsights(req).getInsights();

        return ClassDashboardResponseDTO.builder()
                .classId(classId.toString())
                .className(className)
                .totalStudents(totalStudents)
                .avgRetentionRate(avgRetentionRate)
                .totalReviewedToday(totalReviewedToday)
                .weakTopics(weakTopics)
                .students(students)
                .insights(insights)
                .build();
    }
}
