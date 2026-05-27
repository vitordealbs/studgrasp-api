package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.flashcard.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<FlashcardResponseDTO> create(@RequestBody @Valid FlashcardRequestDTO dto) {
        var response = flashcardService.createFlashcard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{flashcardId}/attempts")
    public ResponseEntity<FlashcardAttemptResponseDTO> totalAttempt(
            @PathVariable UUID flashcardId,
            @RequestBody @Valid FlashcardAttemptRequestDTO dto) {
        var response = flashcardService.totalAttempt(flashcardId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}