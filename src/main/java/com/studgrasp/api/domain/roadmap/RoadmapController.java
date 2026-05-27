package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping
    public ResponseEntity<List<RoadmapResponseDTO>> getAll() {
        var response = roadmapService.getAllRoadmaps();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponseDTO> getById(@PathVariable UUID id) {
        var response = roadmapService.getRoadmapWithNodes(id);
        return ResponseEntity.ok(response);
    }
}