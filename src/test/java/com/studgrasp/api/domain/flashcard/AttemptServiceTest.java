package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.AttemptRequestDTO;
import com.studgrasp.api.domain.flashcard.dto.AttemptResponseDTO;
import com.studgrasp.api.domain.flashcard.dto.WeakTopicDTO;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @InjectMocks
    private AttemptService attemptService;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardAttemptRepository attemptRepository;

    private User user() {
        return User.builder().id(UUID.randomUUID()).name("Test").build();
    }

    private Flashcard flashcard(UUID id, UUID nodeId) {
        return Flashcard.builder()
                .id(id).nodeId(nodeId).question("Q").answer("A")
                .difficulty("EASY").aiGenerated(false).build();
    }

    @Test
    void shouldCreateNewAttemptWhenNoneExists() {
        UUID flashcardId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        User user = user();
        Flashcard card = flashcard(flashcardId, nodeId);
        AttemptRequestDTO dto = new AttemptRequestDTO(flashcardId, 4);

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(card));
        when(attemptRepository.findByFlashcardIdAndUserId(flashcardId, user.getId()))
                .thenReturn(Optional.empty());
        when(attemptRepository.save(any())).thenAnswer(inv -> {
            FlashcardAttempt a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AttemptResponseDTO result = attemptService.recordAttempt(user, dto);

        assertNotNull(result);
        assertEquals(flashcardId, result.id());
        assertEquals(1, result.repetitions());
        verify(attemptRepository).save(any());
    }

    @Test
    void shouldUpdateExistingAttemptOnUpsert() {
        UUID flashcardId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        User user = user();
        Flashcard card = flashcard(flashcardId, nodeId);
        AttemptRequestDTO dto = new AttemptRequestDTO(flashcardId, 5);

        FlashcardAttempt existing = FlashcardAttempt.builder()
                .id(UUID.randomUUID()).flashcard(card).user(user)
                .repetitions(1).intervalDays(1).easeFactor(2.5)
                .correct(true).answeredAt(LocalDateTime.now())
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .build();

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(card));
        when(attemptRepository.findByFlashcardIdAndUserId(flashcardId, user.getId()))
                .thenReturn(Optional.of(existing));
        when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AttemptResponseDTO result = attemptService.recordAttempt(user, dto);

        assertEquals(2, result.repetitions());
        assertEquals(6, result.intervalDays());
    }

    @Test
    void shouldResetRepetitionsWhenQualityIsLow() {
        UUID flashcardId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        User user = user();
        Flashcard card = flashcard(flashcardId, nodeId);
        AttemptRequestDTO dto = new AttemptRequestDTO(flashcardId, 1);

        FlashcardAttempt existing = FlashcardAttempt.builder()
                .id(UUID.randomUUID()).flashcard(card).user(user)
                .repetitions(5).intervalDays(21).easeFactor(2.5)
                .correct(true).answeredAt(LocalDateTime.now())
                .nextReviewAt(LocalDateTime.now().plusDays(21))
                .build();

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(card));
        when(attemptRepository.findByFlashcardIdAndUserId(flashcardId, user.getId()))
                .thenReturn(Optional.of(existing));
        when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AttemptResponseDTO result = attemptService.recordAttempt(user, dto);

        assertEquals(0, result.repetitions());
        assertEquals(1, result.intervalDays());
    }

    @Test
    void shouldThrowWhenFlashcardNotFound() {
        UUID flashcardId = UUID.randomUUID();
        User user = user();
        AttemptRequestDTO dto = new AttemptRequestDTO(flashcardId, 3);

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attemptService.recordAttempt(user, dto));
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void shouldReturnDueFlashcards() {
        UUID userId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        Flashcard card = flashcard(UUID.randomUUID(), nodeId);
        FlashcardAttempt attempt = FlashcardAttempt.builder()
                .id(UUID.randomUUID()).flashcard(card)
                .nextReviewAt(LocalDateTime.now().minusHours(1))
                .answeredAt(LocalDateTime.now()).correct(true)
                .build();

        when(attemptRepository.findDueForReview(eq(userId), any())).thenReturn(List.of(attempt));

        List<AttemptResponseDTO> result = attemptService.getDueFlashcards(userId);

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnWeakTopicsForAnalysis() {
        UUID userId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        Object[] row = new Object[]{nodeId.toString(), "Recursão", 10L, 6L};

        when(attemptRepository.findWeakTopicsByUserId(userId)).thenReturn(List.<Object[]>of(row));

        var result = attemptService.getAnalysis(userId);

        assertEquals(1, result.weakTopics().size());
        WeakTopicDTO topic = result.weakTopics().get(0);
        assertEquals("Recursão", topic.nodeTitle());
        assertEquals(0.6, topic.errorRate(), 0.001);
    }
}
