package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.dashboard.dto.DashboardResponseDTO;
import com.studgrasp.api.domain.dashboard.dto.InsightResponseDTO;
import com.studgrasp.api.domain.flashcard.FlashcardAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private FlashcardAttemptRepository repo;

    @Mock
    private AiInsightsClient aiClient;

    private Object[] rawStats(int reviewedToday, int correctToday, int dueNow,
                              String nextReview, int mastered, Double retention) {
        return new Object[]{reviewedToday, correctToday, dueNow, nextReview, mastered, retention};
    }

    @Test
    void shouldReturnDashboardWithCorrectStats() {
        UUID userId = UUID.randomUUID();
        Object[] raw = rawStats(5, 3, 2, null, 10, 0.75);

        when(repo.findRawStatsByUserId(userId)).thenReturn(raw);
        when(repo.findActivityDatesByUserId(userId)).thenReturn(List.of(Date.valueOf(LocalDate.now())));
        when(repo.findWeakTopicsByUserId(userId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchInsights(any())).thenReturn(new InsightResponseDTO(userId.toString(), List.of("Keep it up!")));

        DashboardResponseDTO result = dashboardService.getDashboard(userId);

        assertEquals(userId.toString(), result.getUserId());
        assertEquals(5, result.getStats().getReviewedToday());
        assertEquals(3, result.getStats().getCorrectToday());
        assertEquals(2, result.getStats().getDueNow());
        assertEquals(10, result.getStats().getTotalMastered());
        assertEquals(0.75, result.getStats().getRetentionRate());
        assertEquals(1, result.getStats().getStreak());
        assertEquals(1, result.getInsights().size());
    }

    @Test
    void shouldCalculateStreakFromConsecutiveDays() {
        UUID userId = UUID.randomUUID();
        Object[] raw = rawStats(0, 0, 0, null, 0, null);
        List<Date> dates = List.of(
                Date.valueOf(LocalDate.now()),
                Date.valueOf(LocalDate.now().minusDays(1)),
                Date.valueOf(LocalDate.now().minusDays(2))
        );

        when(repo.findRawStatsByUserId(userId)).thenReturn(raw);
        when(repo.findActivityDatesByUserId(userId)).thenReturn(dates);
        when(repo.findWeakTopicsByUserId(userId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchInsights(any())).thenReturn(new InsightResponseDTO(userId.toString(), Collections.emptyList()));

        DashboardResponseDTO result = dashboardService.getDashboard(userId);

        assertEquals(3, result.getStats().getStreak());
    }

    @Test
    void shouldReturnZeroStreakWhenNoRecentActivity() {
        UUID userId = UUID.randomUUID();
        Object[] raw = rawStats(0, 0, 0, null, 0, null);
        List<Date> oldDates = List.of(Date.valueOf(LocalDate.now().minusDays(5)));

        when(repo.findRawStatsByUserId(userId)).thenReturn(raw);
        when(repo.findActivityDatesByUserId(userId)).thenReturn(oldDates);
        when(repo.findWeakTopicsByUserId(userId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchInsights(any())).thenReturn(new InsightResponseDTO(userId.toString(), Collections.emptyList()));

        DashboardResponseDTO result = dashboardService.getDashboard(userId);

        assertEquals(0, result.getStats().getStreak());
    }

    @Test
    void shouldReturnEmptyInsightsWhenAiFails() {
        UUID userId = UUID.randomUUID();
        Object[] raw = rawStats(0, 0, 0, null, 0, null);

        when(repo.findRawStatsByUserId(userId)).thenReturn(raw);
        when(repo.findActivityDatesByUserId(userId)).thenReturn(Collections.emptyList());
        when(repo.findWeakTopicsByUserId(userId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchInsights(any())).thenReturn(new InsightResponseDTO(userId.toString(), Collections.emptyList()));

        DashboardResponseDTO result = dashboardService.getDashboard(userId);

        assertTrue(result.getInsights().isEmpty());
    }

    @Test
    void shouldHandleNullRetentionRateGracefully() {
        UUID userId = UUID.randomUUID();
        Object[] raw = rawStats(0, 0, 0, null, 0, null);

        when(repo.findRawStatsByUserId(userId)).thenReturn(raw);
        when(repo.findActivityDatesByUserId(userId)).thenReturn(Collections.emptyList());
        when(repo.findWeakTopicsByUserId(userId)).thenReturn(Collections.emptyList());
        when(aiClient.fetchInsights(any())).thenReturn(new InsightResponseDTO(userId.toString(), Collections.emptyList()));

        DashboardResponseDTO result = dashboardService.getDashboard(userId);

        assertNull(result.getStats().getRetentionRate());
    }
}
