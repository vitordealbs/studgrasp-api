package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.flashcard.dto.*;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Flashcards", description = "Flashcard creation and spaced-repetition review")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @Operation(summary = "Create a flashcard linked to a roadmap node")
    @PostMapping
    public ResponseEntity<FlashcardResponseDTO> create(
            @RequestBody @Valid FlashcardRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashcardService.createFlashcard(dto));
    }

    @Operation(summary = "Record a flashcard review attempt")
    @PostMapping("/{flashcardId}/attempts")
    public ResponseEntity<FlashcardAttemptResponseDTO> recordAttempt(
            @PathVariable UUID flashcardId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid FlashcardAttemptRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashcardService.recordAttempt(flashcardId, user, dto));
    }

    @Operation(summary = "List flashcards due for review for the authenticated user")
    @GetMapping("/due")
    public ResponseEntity<List<FlashcardAttemptResponseDTO>> getDue(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(flashcardService.getDueFlashcards(user));
    }
}
