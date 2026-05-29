package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.flashcard.dto.AnalysisResponseDTO;
import com.studgrasp.api.domain.flashcard.dto.AttemptRequestDTO;
import com.studgrasp.api.domain.flashcard.dto.AttemptResponseDTO;
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

@Tag(name = "Attempts", description = "Spaced-repetition scheduling via SM-2 algorithm")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @Operation(summary = "Record a flashcard attempt and recalculate SM-2 schedule")
    @PostMapping
    public ResponseEntity<AttemptResponseDTO> recordAttempt(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AttemptRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attemptService.recordAttempt(user, dto));
    }

    @Operation(summary = "Get flashcards due for review for a given user")
    @GetMapping("/due/{userId}")
    public ResponseEntity<List<AttemptResponseDTO>> getDue(@PathVariable UUID userId) {
        return ResponseEntity.ok(attemptService.getDueFlashcards(userId));
    }

    @Operation(summary = "Get weak topics analysis for a given user")
    @GetMapping("/analysis/{userId}")
    public ResponseEntity<AnalysisResponseDTO> getAnalysis(@PathVariable UUID userId) {
        return ResponseEntity.ok(attemptService.getAnalysis(userId));
    }
}
