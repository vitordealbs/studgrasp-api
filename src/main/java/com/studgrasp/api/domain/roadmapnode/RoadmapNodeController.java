package com.studgrasp.api.domain.roadmapnode;

import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeRequestDTO;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roadmap-nodes")
@RequiredArgsConstructor
public class RoadmapNodeController {

    private final RoadmapNodeService roadmapNodeService;

    @PostMapping
    public ResponseEntity<RoadmapNodeResponseDTO> create(
            @RequestBody @Valid RoadmapNodeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapNodeService.create(request));
    }
}