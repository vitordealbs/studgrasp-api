package com.studgrasp.api.domain.session;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.session.dto.StudySessionRequestDTO;
import com.studgrasp.api.domain.session.dto.StudySessionResponseDTO;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Start a new study session")
    @PostMapping
    public ResponseEntity<StudySessionResponseDTO> startSession(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid StudySessionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studySessionService.startSession(user, dto));
    }

    @Operation(summary = "End an active study session")
    @PatchMapping("/{sessionId}/end")
    public ResponseEntity<StudySessionResponseDTO> endSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User user,
            @RequestParam @NotNull LocalDateTime endedAt) {
        return ResponseEntity.ok(studySessionService.endSession(sessionId, user, endedAt));
    }
}
