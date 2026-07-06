package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.roadmap.dto.RoadmapRequestDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import com.studgrasp.api.domain.user.User;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @Operation(summary = "Create custom roadmap", description = "Authenticated users can create their own custom roadmaps and optionally publish them.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "201", description = "Roadmap created successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping("/custom")
    public ResponseEntity<RoadmapResponseDTO> createCustom(
            @RequestBody @Valid RoadmapRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapService.createCustom(request, user.getId()));
    }

    @Operation(summary = "Get my roadmaps", description = "Returns roadmaps created by the authenticated user and roadmaps they have saved.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "200", description = "Roadmaps listed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/my")
    public ResponseEntity<List<RoadmapResponseDTO>> getMyRoadmaps(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(roadmapService.getMyRoadmaps(user.getId()));
    }

    @Operation(summary = "Get workshop roadmaps", description = "Returns all public roadmaps created by users. This is the 'workshop' where users can discover and save roadmaps created by the community.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "200", description = "Workshop roadmaps listed successfully")
    @GetMapping("/workshop")
    public ResponseEntity<List<RoadmapResponseDTO>> getWorkshop(@AuthenticationPrincipal User user) {
        UUID userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(roadmapService.getPublicRoadmaps(userId));
    }

    @Operation(summary = "Save a public roadmap", description = "Saves a public roadmap to the authenticated user's collection.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "200", description = "Roadmap saved successfully")
    @ApiResponse(responseCode = "400", description = "Roadmap is not public")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @PostMapping("/{id}/save")
    public ResponseEntity<Void> saveRoadmap(
            @Parameter(description = "Roadmap UUID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        roadmapService.saveRoadmap(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unsave a roadmap", description = "Removes a roadmap from the authenticated user's saved collection.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "204", description = "Roadmap unsaved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @DeleteMapping("/{id}/save")
    public ResponseEntity<Void> unsaveRoadmap(
            @Parameter(description = "Roadmap UUID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        roadmapService.unsaveRoadmap(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add coauthor to roadmap", description = "Invites another user to collaborate on a custom roadmap. Only the roadmap creator can add coauthors.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "200", description = "Coauthor added successfully")
    @ApiResponse(responseCode = "400", description = "Invalid email or user already a coauthor")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Only roadmap creator can add coauthors")
    @ApiResponse(responseCode = "404", description = "Roadmap or user not found")
    @PostMapping("/{id}/coauthors")
    public ResponseEntity<Void> addCoauthor(
            @Parameter(description = "Roadmap UUID") @PathVariable UUID id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal User user) {
        String email = body.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        roadmapService.addCoauthor(id, email.trim(), user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove coauthor from roadmap", description = "Removes a collaborator from the roadmap. Only the roadmap creator can remove coauthors.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "204", description = "Coauthor removed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Only roadmap creator can remove coauthors")
    @DeleteMapping("/{id}/coauthors/{coauthorId}")
    public ResponseEntity<Void> removeCoauthor(
            @Parameter(description = "Roadmap UUID") @PathVariable UUID id,
            @Parameter(description = "Coauthor User UUID") @PathVariable UUID coauthorId,
            @AuthenticationPrincipal User user) {
        roadmapService.removeCoauthor(id, coauthorId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get roadmap coauthors", description = "Returns the list of coauthors for a custom roadmap.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "200", description = "Coauthors listed successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @GetMapping("/{id}/coauthors")
    public ResponseEntity<List<com.studgrasp.api.domain.roadmap.dto.CoauthorDTO>> getCoauthors(
            @Parameter(description = "Roadmap UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(roadmapService.getCoauthors(id));
    }

    @Operation(summary = "Clone a roadmap", description = "Creates a personal copy of any roadmap (public or your own) that you can edit and add flashcards to. The cloned roadmap will have all the same nodes but you will be the owner.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME))
    @ApiResponse(responseCode = "201", description = "Roadmap cloned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @PostMapping("/{id}/clone")
    public ResponseEntity<RoadmapResponseDTO> cloneRoadmap(
            @Parameter(description = "Roadmap UUID to clone") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapService.cloneRoadmap(id, user.getId()));
    }
}
