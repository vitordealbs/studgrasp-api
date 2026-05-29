package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.flashcard.dto.AnalysisResponseDTO;
import com.studgrasp.api.domain.flashcard.dto.AttemptRequestDTO;
import com.studgrasp.api.domain.flashcard.dto.AttemptResponseDTO;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.security.OwnershipValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    private final OwnershipValidator ownershipValidator;

    @Operation(summary = "Record a flashcard attempt and recalculate SM-2 schedule", description = "Persists the review quality score and updates interval, easiness factor, and next review date using the SM-2 algorithm")
    @ApiResponse(responseCode = "201", description = "Attempt recorded and SM-2 schedule recalculated")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping
    public ResponseEntity<AttemptResponseDTO> recordAttempt(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AttemptRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attemptService.recordAttempt(user, dto));
    }

    @Operation(summary = "Get flashcards due for review for a given user", description = "Returns all flashcards whose SM-2 next review date is today or earlier for the specified user")
    @ApiResponse(responseCode = "200", description = "Due flashcards returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Access denied — not the owner and not an ADVISOR")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/due/{userId}")
    public ResponseEntity<List<AttemptResponseDTO>> getDue(
            @AuthenticationPrincipal User principal,
            @Parameter(description = "UUID of the user") @PathVariable UUID userId) {
        ownershipValidator.requireOwnerOrAdvisor(principal, userId);
        return ResponseEntity.ok(attemptService.getDueFlashcards(userId));
    }

    @Operation(summary = "Get weak topics analysis for a given user", description = "Aggregates attempt history to identify topics with consistently low SM-2 quality scores")
    @ApiResponse(responseCode = "200", description = "Weak topics analysis returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Access denied — not the owner and not an ADVISOR")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/analysis/{userId}")
    public ResponseEntity<AnalysisResponseDTO> getAnalysis(
            @AuthenticationPrincipal User principal,
            @Parameter(description = "UUID of the user") @PathVariable UUID userId) {
        ownershipValidator.requireOwnerOrAdvisor(principal, userId);
        return ResponseEntity.ok(attemptService.getAnalysis(userId));
    }
}
