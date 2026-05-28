package com.studgrasp.api.domain.roadmapnode;

import com.studgrasp.api.domain.roadmap.Roadmap;
import com.studgrasp.api.domain.roadmap.RoadmapNode;
import com.studgrasp.api.domain.roadmap.RoadmapRepository;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeRequestDTO;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeResponseDTO;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoadmapNodeService {

    private final RoadmapNodeRepository roadmapNodeRepository;
    private final RoadmapRepository roadmapRepository;

    public RoadmapNodeResponseDTO create(RoadmapNodeRequestDTO request) {
        Roadmap roadmap = roadmapRepository.findById(request.roadmapId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Roadmap not found: " + request.roadmapId()));

        RoadmapNode node = RoadmapNode.builder()
                .roadmap(roadmap)
                .parentId(request.parentId())
                .title(request.title())
                .description(request.description())
                .nodeType(request.nodeType())
                .nodeOrder(request.nodeOrder())
                .build();

        RoadmapNode saved = roadmapNodeRepository.save(node);
        return toDTO(saved);
    }

    private RoadmapNodeResponseDTO toDTO(RoadmapNode node) {
        return new RoadmapNodeResponseDTO(
                node.getId(),
                node.getTitle(),
                node.getDescription(),
                node.getParentId(),
                node.getNodeType(),
                node.getNodeOrder(),
                node.getRoadmap().getId()
        );
    }
}
