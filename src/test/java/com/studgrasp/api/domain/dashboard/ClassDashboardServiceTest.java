package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.classroom.Classroom;
import com.studgrasp.api.domain.classroom.ClassroomRepository;
import com.studgrasp.api.domain.dashboard.dto.ClassDashboardResponseDTO;
import com.studgrasp.api.domain.dashboard.dto.ClassInsightResponseDTO;
import com.studgrasp.api.domain.flashcard.FlashcardAttemptRepository;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassDashboardServiceTest {

    @InjectMocks
    private ClassDashboardService service;

    @Mock
    private FlashcardAttemptRepository attemptRepo;

    @Mock
    private ClassroomRepository classroomRepo;

    @Mock
    private AiInsightsClient aiClient;

    private Object[] studentRow(String userId, String name, int reviewedToday, Double retention) {
        return new Object[]{userId, name, reviewedToday, retention};
    }

    private Object[] weakTopicRow(String nodeId, String title, double errorRate) {
        return new Object[]{nodeId, title, errorRate};
    }

    @Test
    void shouldReturnClassDashboardWithCorrectAggregations() {
        UUID classId = UUID.randomUUID();
        Classroom classroom = Classroom.builder().id(classId).name("Eng. Software").build();

        List<Object[]> students = List.<Object[]>of(
                studentRow(UUID.randomUUID().toString(), "Ana",   8, 0.80),
                studentRow(UUID.randomUUID().toString(), "Bruno", 4, 0.60)
        );
        List<Object[]> weakTopics = List.<Object[]>of(
                weakTopicRow(UUID.randomUUID().toString(), "Ponteiros", 0.65)
        );

        when(classroomRepo.findById(classId)).thenReturn(Optional.of(classroom));
        when(attemptRepo.findStudentStatsByClassId(classId)).thenReturn(students);
        when(attemptRepo.findWeakTopicsByClassId(classId)).thenReturn(weakTopics);
        when(aiClient.fetchClassInsights(any()))
                .thenReturn(new ClassInsightResponseDTO(classId.toString(), List.of("Reforçar Ponteiros")));

        ClassDashboardResponseDTO result = service.getDashboard(classId);

        assertEquals(classId.toString(), result.getClassId());
        assertEquals("Eng. Software", result.getClassName());
        assertEquals(2, result.getTotalStudents());
        assertEquals(12, result.getTotalReviewedToday());
        assertEquals(0.70, result.getAvgRetentionRate(), 0.001);
        assertEquals(1, result.getWeakTopics().size());
        assertEquals(0.65, result.getWeakTopics().get(0).errorRate(), 0.001);
        assertEquals(2, result.getStudents().size());
        assertEquals(1, result.getInsights().size());
    }

    @Test
    void shouldReturnNullAvgRetentionRateWhenNoStudentHasHistory() {
        UUID classId = UUID.randomUUID();
        Classroom classroom = Classroom.builder().id(classId).name("Turma A").build();

        when(classroomRepo.findById(classId)).thenReturn(Optional.of(classroom));
        when(attemptRepo.findStudentStatsByClassId(classId))
                .thenReturn(List.<Object[]>of(studentRow(UUID.randomUUID().toString(), "Carlos", 0, null)));
        when(attemptRepo.findWeakTopicsByClassId(classId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchClassInsights(any()))
                .thenReturn(new ClassInsightResponseDTO(classId.toString(), Collections.emptyList()));

        ClassDashboardResponseDTO result = service.getDashboard(classId);

        assertNull(result.getAvgRetentionRate());
    }

    @Test
    void shouldThrowWhenClassNotFound() {
        UUID classId = UUID.randomUUID();
        when(classroomRepo.findById(classId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getDashboard(classId));
        verify(attemptRepo, never()).findStudentStatsByClassId(any());
    }

    @Test
    void shouldReturnEmptyInsightsWhenAiFails() {
        UUID classId = UUID.randomUUID();
        Classroom classroom = Classroom.builder().id(classId).name("Turma B").build();

        when(classroomRepo.findById(classId)).thenReturn(Optional.of(classroom));
        when(attemptRepo.findStudentStatsByClassId(classId)).thenReturn(Collections.emptyList());
        when(attemptRepo.findWeakTopicsByClassId(classId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchClassInsights(any()))
                .thenReturn(new ClassInsightResponseDTO(classId.toString(), Collections.emptyList()));

        ClassDashboardResponseDTO result = service.getDashboard(classId);

        assertTrue(result.getInsights().isEmpty());
    }
}
