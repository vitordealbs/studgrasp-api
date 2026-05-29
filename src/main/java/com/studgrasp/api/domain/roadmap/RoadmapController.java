package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.roadmap.dto.RoadmapRequestDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Roadmaps", description = "Career roadmaps and their content nodes")
@RestController
@RequestMapping("/api/v1/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @Operation(summary = "Create a roadmap (ADVISOR or SCRAPER only)", description = "Creates a new career roadmap. Restricted to advisors and the scraper service.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "201", description = "Roadmap created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Requires ADVISOR or SCRAPER role")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADVISOR', 'SCRAPER')")
    public ResponseEntity<RoadmapResponseDTO> create(
            @RequestBody @Valid RoadmapRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapService.create(request));
    }

    @Operation(summary = "List all roadmaps (paginated)", description = "Public endpoint. Returns a paginated list of all available roadmaps. Default page size is 20.")
    @ApiResponse(responseCode = "200", description = "Roadmaps listed successfully")
    @GetMapping
    public ResponseEntity<Page<RoadmapResponseDTO>> getAll(
            @Parameter(description = "Pagination parameters: page, size, sort") @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(roadmapService.getAllRoadmaps(pageable));
    }

    @Operation(summary = "Get roadmap with its nodes by ID", description = "Public endpoint. Returns the full roadmap including all ordered content nodes.")
    @ApiResponse(responseCode = "200", description = "Roadmap found")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponseDTO> getById(
            @Parameter(description = "Roadmap UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(roadmapService.getRoadmapWithNodes(id));
    }

    @Operation(summary = "Get roadmap by career type", description = "Public endpoint. Returns the roadmap associated with the specified career type slug (e.g. 'backend', 'frontend').")
    @ApiResponse(responseCode = "200", description = "Roadmap found")
    @ApiResponse(responseCode = "404", description = "No roadmap found for the given career type")
    @GetMapping("/career/{careerType}")
    public ResponseEntity<RoadmapResponseDTO> getByCareerType(
            @Parameter(description = "Career type slug (e.g. backend, frontend, devops)") @PathVariable String careerType) {
        return ResponseEntity.ok(roadmapService.getByCareerType(careerType));
    }
}
