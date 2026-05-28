package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.*;
import com.studgrasp.api.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<FlashcardResponseDTO> create(
            @RequestBody @Valid FlashcardRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashcardService.createFlashcard(dto));
    }

    @PostMapping("/{flashcardId}/attempts")
    public ResponseEntity<FlashcardAttemptResponseDTO> recordAttempt(
            @PathVariable UUID flashcardId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid FlashcardAttemptRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashcardService.recordAttempt(flashcardId, user, dto));
    }

    @GetMapping("/due")
    public ResponseEntity<List<FlashcardAttemptResponseDTO>> getDue(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(flashcardService.getDueFlashcards(user));
    }
}
