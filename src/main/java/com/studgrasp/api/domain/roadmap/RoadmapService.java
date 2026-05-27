package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.roadmap.dto.RoadmapNodeResponseDTO;
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

    @Transactional(readOnly = true)
    public List<RoadmapResponseDTO> getAllRoadmaps() {
        return roadmapRepository.findAll().stream()
                .map(roadmap -> new RoadmapResponseDTO(
                        roadmap.getId(),
                        roadmap.getTitle(),
                        roadmap.getCareerType(),
                        roadmap.getSourceUrl(),
                        List.of()
                )).toList();
    }

    @Transactional(readOnly = true)
    public RoadmapResponseDTO getRoadmapWithNodes(UUID roadmapId) {
        var roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));

        var nodes = roadmapNodeRepository.findByRoadmapIdOrderByNodeOrderAsc(roadmapId).stream()
                .map(node -> new RoadmapNodeResponseDTO(
                        node.getId(),
                        node.getParentId(),
                        node.getTitle(),
                        node.getDescription(),
                        node.getNodeOrder(),
                        node.getNodeType()
                )).toList();

        return new RoadmapResponseDTO(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getCareerType(),
                roadmap.getSourceUrl(),
                nodes
        );
    }
}