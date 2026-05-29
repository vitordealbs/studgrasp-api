package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.dashboard.dto.AdvisorDashboardResponseDTO;
import com.studgrasp.api.domain.dashboard.dto.ClassInsightResponseDTO;
import com.studgrasp.api.domain.flashcard.FlashcardAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorDashboardServiceTest {

    @InjectMocks
    private AdvisorDashboardService service;

    @Mock
    private FlashcardAttemptRepository repo;

    @Mock
    private AiInsightsClient aiClient;

    private Object[] studentRow(String userId, String name, int reviewedToday, Double retention) {
        return new Object[]{userId, name, reviewedToday, retention};
    }

    private Object[] weakTopicRow(String nodeId, String title, long total, long wrong) {
        return new Object[]{nodeId, title, total, wrong};
    }

    @Test
    void shouldReturnAdvisorDashboardWithAggregatedStats() {
        UUID classId = UUID.randomUUID();
        List<Object[]> students = List.<Object[]>of(
                studentRow(UUID.randomUUID().toString(), "Ana",  10, 0.80),
                studentRow(UUID.randomUUID().toString(), "Bruno", 5, 0.60)
        );
        List<Object[]> weakTopics = List.<Object[]>of(
                weakTopicRow(UUID.randomUUID().toString(), "Ponteiros", 20L, 12L)
        );

        when(repo.findStudentStatsByClassId(classId)).thenReturn(students);
        when(repo.findWeakTopicsByClassId(classId)).thenReturn(weakTopics);
        when(aiClient.fetchClassInsights(any()))
                .thenReturn(new ClassInsightResponseDTO(classId.toString(), List.of("Foco em Ponteiros")));

        AdvisorDashboardResponseDTO result = service.getDashboard(classId);

        assertEquals(classId.toString(), result.getClassId());
        assertEquals(2, result.getClassStats().getTotalStudents());
        assertEquals(15, result.getClassStats().getTotalReviewedToday());
        assertEquals(0.70, result.getClassStats().getAvgRetentionRate(), 0.001);
        assertEquals(1, result.getWeakTopics().size());
        assertEquals(2, result.getStudents().size());
        assertEquals(1, result.getInsights().size());
    }

    @Test
    void shouldReturnNullAvgRetentionRateWhenNoStudentHasHistory() {
        UUID classId = UUID.randomUUID();
        List<Object[]> students = List.<Object[]>of(
                studentRow(UUID.randomUUID().toString(), "Carlos", 0, null)
        );

        when(repo.findStudentStatsByClassId(classId)).thenReturn(students);
        when(repo.findWeakTopicsByClassId(classId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchClassInsights(any()))
                .thenReturn(new ClassInsightResponseDTO(classId.toString(), Collections.emptyList()));

        AdvisorDashboardResponseDTO result = service.getDashboard(classId);

        assertNull(result.getClassStats().getAvgRetentionRate());
    }

    @Test
    void shouldReturnEmptyInsightsWhenAiFails() {
        UUID classId = UUID.randomUUID();

        when(repo.findStudentStatsByClassId(classId)).thenReturn(Collections.emptyList());
        when(repo.findWeakTopicsByClassId(classId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchClassInsights(any()))
                .thenReturn(new ClassInsightResponseDTO(classId.toString(), Collections.emptyList()));

        AdvisorDashboardResponseDTO result = service.getDashboard(classId);

        assertTrue(result.getInsights().isEmpty());
        assertEquals(0, result.getClassStats().getTotalStudents());
    }
}
