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
    private final com.studgrasp.api.domain.roadmap.RoadmapService roadmapService;

    public RoadmapNodeResponseDTO create(RoadmapNodeRequestDTO request, java.util.UUID userId) {
        Roadmap roadmap = roadmapRepository.findById(request.roadmapId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Roadmap not found: " + request.roadmapId()));

        // Check permissions: only creator, coauthors, ADVISOR or SCRAPER can add nodes
        if (userId != null && roadmap.isCustom()) {
            if (!roadmapService.canEditRoadmap(request.roadmapId(), userId)) {
                throw new IllegalArgumentException(
                    "Only the roadmap creator and coauthors can add nodes to this roadmap");
            }
        }

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
