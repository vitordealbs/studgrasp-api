package com.studgrasp.api.domain.session;

import com.studgrasp.api.domain.session.dto.StudySessionRequestDTO;
import com.studgrasp.api.domain.session.dto.StudySessionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping("/users/{userId}")
    public ResponseEntity<StudySessionResponseDTO> startSession(
            @PathVariable UUID userId,
            @RequestBody @Valid StudySessionRequestDTO dto) {
        var response = studySessionService.startSession(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{sessionId}/end")
    public ResponseEntity<StudySessionResponseDTO> endSession(
            @PathVariable UUID sessionId,
            @RequestParam LocalDateTime endedAt) {
        var response = studySessionService.endSession(sessionId, endedAt);
        return ResponseEntity.ok(response);
    }
}