package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Roadmap roadmap = Roadmap.builder().id(UUID.randomUUID()).title("Java Dev").careerType("BACKEND").sourceUrl("http://test.com").build();
        when(roadmapRepository.findAll()).thenReturn(List.of(roadmap));

        List<RoadmapResponseDTO> response = roadmapService.getAllRoadmaps();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Java Dev", response.get(0).title());
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
    void shouldThrowEntityNotFoundExceptionWhenRoadmapDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(roadmapRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roadmapService.getRoadmapWithNodes(id));
    }
}