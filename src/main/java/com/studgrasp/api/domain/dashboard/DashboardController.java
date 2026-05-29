package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.dashboard.dto.DashboardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Dashboard", description = "Student personal dashboard with AI-generated insights and SM-2 stats")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @Operation(summary = "Get student dashboard", description = "Returns study stats, weak topics, and AI-generated insights for the given user")
    @ApiResponse(responseCode = "200", description = "Dashboard data returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{userId}")
    public ResponseEntity<DashboardResponseDTO> getDashboard(
            @Parameter(description = "UUID of the student") @PathVariable UUID userId) {
        return ResponseEntity.ok(service.getDashboard(userId));
    }
}
