package com.studgrasp.api.domain.progress;

import com.studgrasp.api.domain.progress.dto.UserProgressRequestDTO;
import com.studgrasp.api.domain.progress.dto.UserProgressResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService userProgressService;

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserProgressResponseDTO> updateProgress(
            @PathVariable UUID userId,
            @RequestBody @Valid UserProgressRequestDTO dto) {
        var response = userProgressService.updateProgress(userId, dto);
        return ResponseEntity.ok(response);
    }
}