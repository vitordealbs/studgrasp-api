package com.studgrasp.api.domain.session;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.session.dto.StudySessionRequestDTO;
import com.studgrasp.api.domain.session.dto.StudySessionResponseDTO;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "Study Sessions", description = "Track study sessions linked to roadmap nodes")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/v1/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @Operation(summary = "Start a new study session", description = "Opens a study session for the authenticated user associated with a specific roadmap node")
    @ApiResponse(responseCode = "201", description = "Study session started successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Roadmap node not found")
    @PostMapping
    public ResponseEntity<StudySessionResponseDTO> startSession(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid StudySessionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studySessionService.startSession(user, dto));
    }

    @Operation(summary = "End an active study session", description = "Closes the specified session by recording the end timestamp, calculating the total duration")
    @ApiResponse(responseCode = "200", description = "Session ended successfully")
    @ApiResponse(responseCode = "400", description = "endedAt is missing or before session start time")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Session belongs to a different user")
    @ApiResponse(responseCode = "404", description = "Session not found")
    @PatchMapping("/{sessionId}/end")
    public ResponseEntity<StudySessionResponseDTO> endSession(
            @Parameter(description = "UUID of the study session to end") @PathVariable UUID sessionId,
            @AuthenticationPrincipal User user,
            @Parameter(description = "Session end timestamp (ISO-8601, e.g. 2025-06-01T14:30:00)") @RequestParam @NotNull LocalDateTime endedAt) {
        return ResponseEntity.ok(studySessionService.endSession(sessionId, user, endedAt));
    }
}
