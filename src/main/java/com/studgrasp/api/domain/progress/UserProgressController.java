package com.studgrasp.api.domain.progress;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.progress.dto.UserProgressRequestDTO;
import com.studgrasp.api.domain.progress.dto.UserProgressResponseDTO;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Progress", description = "Track user progress on roadmap nodes")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService userProgressService;

    @Operation(summary = "Update progress status for a roadmap node")
    @PutMapping
    public ResponseEntity<UserProgressResponseDTO> updateProgress(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UserProgressRequestDTO dto) {
        return ResponseEntity.ok(userProgressService.updateProgress(user, dto));
    }
}
