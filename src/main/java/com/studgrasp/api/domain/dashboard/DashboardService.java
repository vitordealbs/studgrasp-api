package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.dashboard.dto.*;
import com.studgrasp.api.domain.flashcard.FlashcardAttemptRepository;
import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FlashcardAttemptRepository repo;
    private final AiInsightsClient aiClient;

    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboard(UUID userId) {
        List<Object[]> results = repo.findRawStatsByUserId(userId);

        // Query always returns one row with aggregate functions
        Object[] raw = results.isEmpty() ? new Object[6] : results.get(0);

        int reviewedToday = raw[0] != null ? ((Number) raw[0]).intValue() : 0;
        int correctToday  = raw[1] != null ? ((Number) raw[1]).intValue() : 0;
        int dueNow        = raw[2] != null ? ((Number) raw[2]).intValue() : 0;
        String nextReviewAt = raw[3] != null ? raw[3].toString() : null;
        int totalMastered = raw[4] != null ? ((Number) raw[4]).intValue() : 0;
        Double retentionRate = raw[5] != null ? ((Number) raw[5]).doubleValue() : null;

        List<LocalDate> dates = repo.findActivityDatesByUserId(userId)
                .stream().map(Date::toLocalDate).toList();
        int streak = calculateStreak(dates);

        UserStatsDTO stats = UserStatsDTO.builder()
                .streak(streak)
                .reviewedToday(reviewedToday)
                .correctToday(correctToday)
                .dueNow(dueNow)
                .nextReviewAt(nextReviewAt)
                .totalMastered(totalMastered)
                .retentionRate(retentionRate)
                .build();

        List<WeakTopicDTO> weakTopics = repo.findWeakTopicsByUserId(userId)
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

        InsightRequestDTO req = InsightRequestDTO.builder()
                .userId(userId.toString())
                .weakTopics(weakTopics)
                .streak(streak)
                .reviewedToday(reviewedToday)
                .correctToday(correctToday)
                .dueNow(dueNow)
                .retentionRate(retentionRate)
                .build();
        List<String> insights = aiClient.fetchInsights(req).getInsights();

        return DashboardResponseDTO.builder()
                .userId(userId.toString())
                .stats(stats)
                .weakTopics(weakTopics)
                .insights(insights)
                .build();
    }

    private int calculateStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) return 0;
        Set<LocalDate> dateSet = new HashSet<>(dates);
        LocalDate today = LocalDate.now();
        LocalDate start = dateSet.contains(today) ? today
                        : dateSet.contains(today.minusDays(1)) ? today.minusDays(1)
                        : null;
        if (start == null) return 0;
        int streak = 0;
        for (LocalDate d = start; dateSet.contains(d); d = d.minusDays(1)) streak++;
        return streak;
    }
}
