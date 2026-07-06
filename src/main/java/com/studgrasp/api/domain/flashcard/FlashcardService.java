package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.*;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRepository;
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
    private final UserRepository userRepository;
    private final com.studgrasp.api.domain.roadmapnode.RoadmapNodeRepository roadmapNodeRepository;
    private final com.studgrasp.api.domain.roadmap.RoadmapService roadmapService;

    @Transactional
    public FlashcardResponseDTO createFlashcard(FlashcardRequestDTO dto, UUID createdBy) {
        // Validate that the user can create flashcards for this node
        if (createdBy != null && !dto.aiGenerated()) {
            var node = roadmapNodeRepository.findById(dto.nodeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Node not found"));

            var roadmapId = node.getRoadmap().getId();

            // Only roadmap owner or coauthors can create flashcards
            if (!roadmapService.canEditRoadmap(roadmapId, createdBy)) {
                throw new IllegalArgumentException(
                    "You can only create flashcards for roadmaps you own or are a coauthor of. " +
                    "Clone the roadmap first to create your own flashcards.");
            }
        }

        var flashcard = Flashcard.builder()
                .nodeId(dto.nodeId())
                .question(dto.question())
                .answer(dto.answer())
                .difficulty(dto.difficulty())
                .aiGenerated(dto.aiGenerated())
                .createdBy(createdBy)
                .build();

        var saved = flashcardRepository.save(flashcard);

        // If user-created (not AI-generated), create initial attempt for immediate review
        if (!dto.aiGenerated() && createdBy != null) {
            var user = userRepository.findById(createdBy)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            var initialAttempt = FlashcardAttempt.builder()
                    .user(user)
                    .flashcard(saved)
                    .correct(false)
                    .answeredAt(LocalDateTime.now())
                    .nextReviewAt(LocalDateTime.now()) // Available immediately
                    .repetitions(0)
                    .easeFactor(2.5)
                    .intervalDays(0)
                    .quality(0)
                    .build();

            flashcardAttemptRepository.save(initialAttempt);
        }

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

    @Transactional
    public void resetProgressForRoadmap(UUID roadmapId, UUID userId) {
        flashcardAttemptRepository.deleteByUserIdAndRoadmapId(userId, roadmapId);
    }

    @Transactional
    public void resetAllProgress(UUID userId) {
        flashcardAttemptRepository.deleteByUserId(userId);
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
