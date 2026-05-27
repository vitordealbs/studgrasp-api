package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.*;
import com.studgrasp.api.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public FlashcardResponseDTO createFlashcard(FlashcardRequestDTO dto) {
        var flashcard = Flashcard.builder()
                .nodeId(dto.nodeId())
                .question(dto.question())
                .answer(dto.answer())
                .difficulty(dto.difficulty())
                .aiGenerated(dto.aiGenerated())
                .build();

        var saved = flashcardRepository.save(flashcard);
        return new FlashcardResponseDTO(
                saved.getId(), saved.getNodeId(), saved.getQuestion(),
                saved.getAnswer(), saved.getDifficulty(), saved.isAiGenerated(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public FlashcardAttemptResponseDTO totalAttempt(UUID flashcardId, FlashcardAttemptRequestDTO dto) {
        var flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));

        var user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var nextReview = dto.correct() ? LocalDateTime.now().plusDays(3) : LocalDateTime.now().plusDays(1);

        var attempt = FlashcardAttempt.builder()
                .user(user)
                .flashcard(flashcard)
                .correct(dto.correct())
                .nextReviewAt(nextReview)
                .build();

        var saved = flashcardAttemptRepository.save(attempt);
        return new FlashcardAttemptResponseDTO(
                saved.getId(), user.getId(), flashcard.getId(),
                saved.isCorrect(), saved.getAnsweredAt(), saved.getNextReviewAt()
        );
    }
}