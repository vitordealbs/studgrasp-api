package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.*;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @InjectMocks
    private FlashcardService flashcardService;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardAttemptRepository flashcardAttemptRepository;

    @Test
    void shouldCreateFlashcardSuccessfully() {
        UUID nodeId = UUID.randomUUID();
        UUID flashcardId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        FlashcardRequestDTO request = new FlashcardRequestDTO(nodeId, "What is DI?", "Dependency Injection", "EASY", true);
        Flashcard saved = Flashcard.builder()
                .id(flashcardId).nodeId(nodeId).question("What is DI?")
                .answer("Dependency Injection").difficulty("EASY").aiGenerated(true).createdAt(now).build();

        when(flashcardRepository.save(any(Flashcard.class))).thenReturn(saved);

        FlashcardResponseDTO response = flashcardService.createFlashcard(request, null);

        assertNotNull(response);
        assertEquals(flashcardId, response.id());
        assertEquals(nodeId, response.nodeId());
        assertTrue(response.aiGenerated());
        assertEquals(now, response.createdAt());
        verify(flashcardRepository, times(1)).save(any(Flashcard.class));
    }

    @Test
    void shouldSaveAttemptSuccessfullyWhenFlashcardAndUserExist() {
        UUID flashcardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        FlashcardAttemptRequestDTO request = new FlashcardAttemptRequestDTO(true);

        Flashcard flashcard = Flashcard.builder().id(flashcardId).build();
        User user = User.builder().id(userId).name("Alex").build();

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));
        when(flashcardAttemptRepository.save(any(FlashcardAttempt.class))).thenAnswer(inv -> {
            FlashcardAttempt attempt = inv.getArgument(0);
            attempt.setId(attemptId);
            attempt.setAnsweredAt(LocalDateTime.now());
            return attempt;
        });

        FlashcardAttemptResponseDTO response = flashcardService.recordAttempt(flashcardId, user, request);

        assertNotNull(response);
        assertEquals(attemptId, response.id());
        assertTrue(response.correct());
        assertNotNull(response.nextReviewAt());
        verify(flashcardRepository, times(1)).findById(flashcardId);
        verify(flashcardAttemptRepository, times(1)).save(any(FlashcardAttempt.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFlashcardDoesNotExist() {
        UUID flashcardId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).build();
        FlashcardAttemptRequestDTO request = new FlashcardAttemptRequestDTO(true);

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> flashcardService.recordAttempt(flashcardId, user, request));
        verify(flashcardAttemptRepository, never()).save(any());
    }
}
