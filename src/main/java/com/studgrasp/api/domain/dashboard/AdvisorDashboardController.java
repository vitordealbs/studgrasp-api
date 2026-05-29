package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.dashboard.dto.AdvisorDashboardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Dashboard", description = "Student personal dashboard with AI-generated insights and SM-2 stats")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/dashboard/advisor")
@RequiredArgsConstructor
public class AdvisorDashboardController {

    private final AdvisorDashboardService service;

    @Operation(summary = "Get advisor dashboard for a class", description = "Returns class-wide stats, per-student performance, weak topics, and AI insights. Requires ADVISOR role.")
    @ApiResponse(responseCode = "200", description = "Advisor dashboard returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Requires ADVISOR role")
    @ApiResponse(responseCode = "404", description = "Class not found")
    @GetMapping("/{classId}")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<AdvisorDashboardResponseDTO> getDashboard(
            @Parameter(description = "UUID of the classroom") @PathVariable UUID classId) {
        return ResponseEntity.ok(service.getDashboard(classId));
    }
}
