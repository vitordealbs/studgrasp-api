package com.studgrasp.api.domain.progress;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.progress.dto.UserProgressRequestDTO;
import com.studgrasp.api.domain.progress.dto.UserProgressResponseDTO;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "Update progress status for a roadmap node", description = "Marks a roadmap node as not started, in progress, or completed for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Progress updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Roadmap node not found")
    @PutMapping
    public ResponseEntity<UserProgressResponseDTO> updateProgress(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UserProgressRequestDTO dto) {
        return ResponseEntity.ok(userProgressService.updateProgress(user, dto));
    }
}
