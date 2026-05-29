package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.AnalysisResponseDTO;
import com.studgrasp.api.domain.flashcard.dto.AttemptRequestDTO;
import com.studgrasp.api.domain.flashcard.dto.AttemptResponseDTO;
import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardAttemptRepository attemptRepository;

    @Transactional
    public AttemptResponseDTO recordAttempt(User user, AttemptRequestDTO dto) {
        Flashcard flashcard = flashcardRepository.findById(dto.flashcardId())
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", dto.flashcardId().toString()));

        FlashcardAttempt attempt = attemptRepository
                .findByFlashcardIdAndUserId(dto.flashcardId(), user.getId())
                .orElseGet(() -> FlashcardAttempt.builder()
                        .flashcard(flashcard)
                        .user(user)
                        .build());

        applySm2(attempt, dto.quality());
        attempt.setCorrect(dto.quality() >= 3);
        attempt.setQuality(dto.quality());
        attempt.setAnsweredAt(LocalDateTime.now());

        return toDTO(attemptRepository.save(attempt));
    }

    @Transactional(readOnly = true)
    public List<AttemptResponseDTO> getDueFlashcards(UUID userId) {
        return attemptRepository
                .findDueForReview(userId, LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalysisResponseDTO getAnalysis(UUID userId) {
        List<WeakTopicDTO> weakTopics = attemptRepository
                .findWeakTopicsByUserId(userId)
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

        return new AnalysisResponseDTO(userId, weakTopics);
    }

    private void applySm2(FlashcardAttempt attempt, int quality) {
        double easeFactor = attempt.getEaseFactor();
        int repetitions = attempt.getRepetitions();
        int intervalDays = attempt.getIntervalDays();
        int newInterval;
        int newRepetitions;

        if (quality < 3) {
            newRepetitions = 0;
            newInterval = 1;
        } else {
            newRepetitions = repetitions + 1;
            if (repetitions == 0) {
                newInterval = 1;
            } else if (repetitions == 1) {
                newInterval = 6;
            } else {
                newInterval = (int) Math.round(intervalDays * easeFactor);
            }
            easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        }

        attempt.setEaseFactor(Math.max(1.3, easeFactor));
        attempt.setRepetitions(newRepetitions);
        attempt.setIntervalDays(newInterval);
        attempt.setNextReviewAt(LocalDateTime.now().plusDays(newInterval));
    }

    private AttemptResponseDTO toDTO(FlashcardAttempt a) {
        Flashcard f = a.getFlashcard();
        return new AttemptResponseDTO(
                f.getId(),
                f.getNodeId(),
                f.getQuestion(),
                f.getAnswer(),
                f.getDifficulty(),
                a.getNextReviewAt(),
                a.getEaseFactor(),
                a.getIntervalDays(),
                a.getRepetitions()
        );
    }
}
