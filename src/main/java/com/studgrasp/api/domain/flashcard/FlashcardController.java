package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.flashcard.dto.*;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "Create a flashcard linked to a roadmap node", description = "Creates a new flashcard associated with a roadmap node. Any authenticated user can create flashcards.")
    @ApiResponse(responseCode = "201", description = "Flashcard created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping
    public ResponseEntity<FlashcardResponseDTO> create(
            @RequestBody @Valid FlashcardRequestDTO dto,
            @AuthenticationPrincipal User user) {
        UUID createdBy = user != null ? user.getId() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashcardService.createFlashcard(dto, createdBy));
    }

    @Operation(summary = "Record a flashcard review attempt", description = "Submits a review result for a flashcard and recalculates the SM-2 schedule for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Attempt recorded and SM-2 schedule updated")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Flashcard not found")
    @PostMapping("/{flashcardId}/attempts")
    public ResponseEntity<FlashcardAttemptResponseDTO> recordAttempt(
            @Parameter(description = "UUID of the flashcard being reviewed") @PathVariable UUID flashcardId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid FlashcardAttemptRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashcardService.recordAttempt(flashcardId, user, dto));
    }

    @Operation(summary = "List flashcards due for review for the authenticated user", description = "Returns all flashcards whose next review date is on or before today, ordered by priority")
    @ApiResponse(responseCode = "200", description = "Due flashcards returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/due")
    public ResponseEntity<List<FlashcardAttemptResponseDTO>> getDue(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(flashcardService.getDueFlashcards(user));
    }

    @Operation(summary = "Reset flashcard progress for a specific roadmap", description = "Deletes all flashcard attempts for the authenticated user for flashcards in a specific roadmap. This allows you to start learning the roadmap from scratch.")
    @ApiResponse(responseCode = "204", description = "Progress reset successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @DeleteMapping("/progress/roadmap/{roadmapId}")
    public ResponseEntity<Void> resetProgressForRoadmap(
            @Parameter(description = "UUID of the roadmap to reset progress for") @PathVariable UUID roadmapId,
            @AuthenticationPrincipal User user) {
        flashcardService.resetProgressForRoadmap(roadmapId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset all flashcard progress", description = "Deletes ALL flashcard attempts for the authenticated user across all roadmaps. Use with caution - this cannot be undone!")
    @ApiResponse(responseCode = "204", description = "All progress reset successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @DeleteMapping("/progress/all")
    public ResponseEntity<Void> resetAllProgress(@AuthenticationPrincipal User user) {
        flashcardService.resetAllProgress(user.getId());
        return ResponseEntity.noContent().build();
    }
}
