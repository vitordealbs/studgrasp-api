package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.classroom.ClassroomRepository;
import com.studgrasp.api.domain.dashboard.dto.ClassDashboardResponseDTO;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Tag(name = "Dashboard", description = "Student personal dashboard with AI-generated insights and SM-2 stats")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class ClassDashboardController {

    private final ClassDashboardService service;
    private final ClassroomRepository classroomRepository;

    @Operation(summary = "Get class dashboard", description = "Returns aggregate retention rate, students ranking, weak topics, and AI insights. Only the classroom's advisor can access this endpoint.")
    @ApiResponse(responseCode = "200", description = "Class dashboard returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not the advisor of this class")
    @ApiResponse(responseCode = "404", description = "Class not found")
    @GetMapping("/class/{classId}")
    public ResponseEntity<ClassDashboardResponseDTO> getClassDashboard(
            @Parameter(description = "UUID of the classroom") @PathVariable UUID classId,
            @AuthenticationPrincipal User user) {

        var classroom = classroomRepository.findByIdWithAdvisor(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));

        if (!classroom.getAdvisor().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the class advisor can access this dashboard");
        }

        return ResponseEntity.ok(service.getDashboard(classId));
    }
}
