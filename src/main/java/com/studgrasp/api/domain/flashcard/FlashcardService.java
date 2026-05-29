package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.*;
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
public class FlashcardService {

    private static final int REVIEW_INTERVAL_CORRECT_DAYS = 3;
    private static final int REVIEW_INTERVAL_WRONG_DAYS = 1;

    private final FlashcardRepository flashcardRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;

    @Transactional
    public FlashcardResponseDTO createFlashcard(FlashcardRequestDTO dto, UUID createdBy) {
        var flashcard = Flashcard.builder()
                .nodeId(dto.nodeId())
                .question(dto.question())
                .answer(dto.answer())
                .difficulty(dto.difficulty())
                .aiGenerated(dto.aiGenerated())
                .createdBy(createdBy)
                .build();

        var saved = flashcardRepository.save(flashcard);
        return toDTO(saved);
    }

    @Transactional
    public FlashcardAttemptResponseDTO recordAttempt(UUID flashcardId, User user,
                                                     FlashcardAttemptRequestDTO dto) {
        var flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        int days = dto.correct() ? REVIEW_INTERVAL_CORRECT_DAYS : REVIEW_INTERVAL_WRONG_DAYS;
        var nextReview = LocalDateTime.now().plusDays(days);

        var attempt = FlashcardAttempt.builder()
                .user(user)
                .flashcard(flashcard)
                .correct(dto.correct())
                .answeredAt(LocalDateTime.now())
                .nextReviewAt(nextReview)
                .build();

        var saved = flashcardAttemptRepository.save(attempt);
        return toAttemptDTO(saved, user.getId());
    }

    @Transactional(readOnly = true)
    public List<FlashcardAttemptResponseDTO> getDueFlashcards(User user) {
        return flashcardAttemptRepository
                .findDueForReview(user.getId(), LocalDateTime.now())
                .stream()
                .map(a -> toAttemptDTO(a, user.getId()))
                .toList();
    }

    private FlashcardResponseDTO toDTO(Flashcard f) {
        return new FlashcardResponseDTO(
                f.getId(), f.getNodeId(), f.getQuestion(),
                f.getAnswer(), f.getDifficulty(), f.isAiGenerated(),
                f.getCreatedAt()
        );
    }

    private FlashcardAttemptResponseDTO toAttemptDTO(FlashcardAttempt a, java.util.UUID userId) {
        return new FlashcardAttemptResponseDTO(
                a.getId(), userId, a.getFlashcard().getId(),
                a.isCorrect(), a.getAnsweredAt(), a.getNextReviewAt()
        );
    }
}
