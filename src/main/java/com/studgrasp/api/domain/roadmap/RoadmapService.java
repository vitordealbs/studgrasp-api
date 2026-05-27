package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.roadmap.dto.RoadmapNodeResponseDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapRequestDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;

    @Transactional
    public RoadmapResponseDTO create(RoadmapRequestDTO request) {
        var roadmap = Roadmap.builder()
                .title(request.title())
                .careerType(request.careerType().toUpperCase())
                .sourceUrl(request.sourceUrl())
                .build();

        roadmapRepository.save(roadmap);
        return toResponse(roadmap, List.of());
    }

    @Transactional(readOnly = true)
    public List<RoadmapResponseDTO> getAllRoadmaps() {
        return roadmapRepository.findAll().stream()
                .map(roadmap -> toResponse(roadmap, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoadmapResponseDTO getRoadmapWithNodes(UUID roadmapId) {
        var roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));

        var nodes = buildNodes(roadmapId);
        return toResponse(roadmap, nodes);
    }

    @Transactional(readOnly = true)
    public RoadmapResponseDTO getByCareerType(String careerType) {
        var roadmap = roadmapRepository.findByCareerTypeIgnoreCase(careerType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Roadmap not found for career: " + careerType));

        var nodes = buildNodes(roadmap.getId());
        return toResponse(roadmap, nodes);
    }

    private List<RoadmapNodeResponseDTO> buildNodes(UUID roadmapId) {
        return roadmapNodeRepository.findByRoadmapIdOrderByNodeOrderAsc(roadmapId)
                .stream()
                .map(node -> new RoadmapNodeResponseDTO(
                        node.getId(),
                        node.getParentId(),
                        node.getTitle(),
                        node.getDescription(),
                        node.getNodeOrder(),
                        node.getNodeType()
                )).toList();
    }

    private RoadmapResponseDTO toResponse(Roadmap roadmap, List<RoadmapNodeResponseDTO> nodes) {
        return new RoadmapResponseDTO(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getCareerType(),
                roadmap.getSourceUrl(),
                nodes
        );
    }
}