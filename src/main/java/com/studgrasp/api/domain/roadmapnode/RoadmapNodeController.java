package com.studgrasp.api.domain.roadmapnode;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeRequestDTO;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Roadmap Nodes", description = "Content nodes within a roadmap")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/v1/roadmap-nodes")
@RequiredArgsConstructor
public class RoadmapNodeController {

    private final RoadmapNodeService roadmapNodeService;

    @Operation(summary = "Create a node in a roadmap (ADVISOR or SCRAPER only)")
    @PostMapping
    public ResponseEntity<RoadmapNodeResponseDTO> create(
            @RequestBody @Valid RoadmapNodeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapNodeService.create(request));
    }
}