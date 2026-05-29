package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.roadmap.dto.RoadmapRequestDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Create a roadmap (ADVISOR or SCRAPER only)", security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADVISOR', 'SCRAPER')")
    public ResponseEntity<RoadmapResponseDTO> create(
            @RequestBody @Valid RoadmapRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapService.create(request));
    }

    @Operation(summary = "List all roadmaps (paginated)")
    @GetMapping
    public ResponseEntity<Page<RoadmapResponseDTO>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(roadmapService.getAllRoadmaps(pageable));
    }

    @Operation(summary = "Get roadmap with its nodes by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(roadmapService.getRoadmapWithNodes(id));
    }

    @Operation(summary = "Get roadmap by career type")
    @GetMapping("/career/{careerType}")
    public ResponseEntity<RoadmapResponseDTO> getByCareerType(
            @PathVariable String careerType) {
        return ResponseEntity.ok(roadmapService.getByCareerType(careerType));
    }
}
