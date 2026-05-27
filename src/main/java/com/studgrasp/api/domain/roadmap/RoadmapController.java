package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.roadmap.dto.RoadmapRequestDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @PostMapping
    public ResponseEntity<RoadmapResponseDTO> create(
            @RequestBody @Valid RoadmapRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RoadmapResponseDTO>> getAll() {
        return ResponseEntity.ok(roadmapService.getAllRoadmaps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(roadmapService.getRoadmapWithNodes(id));
    }

    @GetMapping("/career/{careerType}")
    public ResponseEntity<RoadmapResponseDTO> getByCareerType(
            @PathVariable String careerType) {
        return ResponseEntity.ok(roadmapService.getByCareerType(careerType));
    }
}