package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import com.studgrasp.api.domain.roadmapnode.RoadmapNodeRepository;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceTest {

    @InjectMocks
    private RoadmapService roadmapService;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private RoadmapNodeRepository roadmapNodeRepository;

    @Test
    void shouldReturnAllRoadmaps() {
        var pageable = PageRequest.of(0, 20);
        Roadmap roadmap = Roadmap.builder().id(UUID.randomUUID()).title("Java Dev").careerType("BACKEND").sourceUrl("http://test.com").build();
        when(roadmapRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(roadmap)));

        Page<RoadmapResponseDTO> response = roadmapService.getAllRoadmaps(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Java Dev", response.getContent().get(0).title());
    }

    @Test
    void shouldReturnRoadmapWithNodesWhenIdExists() {
        UUID id = UUID.randomUUID();
        Roadmap roadmap = Roadmap.builder().id(id).title("Java Dev").careerType("BACKEND").build();
        RoadmapNode node = RoadmapNode.builder().id(UUID.randomUUID()).roadmap(roadmap).title("Syntax").nodeOrder(1).nodeType("TOPIC").build();

        when(roadmapRepository.findById(id)).thenReturn(Optional.of(roadmap));
        when(roadmapNodeRepository.findByRoadmapIdOrderByNodeOrderAsc(id)).thenReturn(List.of(node));

        RoadmapResponseDTO response = roadmapService.getRoadmapWithNodes(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals(1, response.nodes().size());
        assertEquals("Syntax", response.nodes().get(0).title());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenRoadmapDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(roadmapRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roadmapService.getRoadmapWithNodes(id));
    }
}
