package com.studgrasp.api.domain.roadmapnode;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeRequestDTO;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeResponseDTO;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Roadmap Nodes", description = "Content nodes within a roadmap")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/v1/roadmap-nodes")
@RequiredArgsConstructor
public class RoadmapNodeController {

    private final RoadmapNodeService roadmapNodeService;

    @Operation(summary = "Create a node in a roadmap", description = "Adds a new content node to an existing roadmap. Roadmap creator and coauthors can add nodes. Nodes represent topics, resources, or milestones within the learning path.")
    @ApiResponse(responseCode = "201", description = "Roadmap node created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Only roadmap creator and coauthors can add nodes")
    @ApiResponse(responseCode = "404", description = "Parent roadmap not found")
    @PostMapping
    public ResponseEntity<RoadmapNodeResponseDTO> create(
            @RequestBody @Valid RoadmapNodeRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapNodeService.create(request, user != null ? user.getId() : null));
    }
}
