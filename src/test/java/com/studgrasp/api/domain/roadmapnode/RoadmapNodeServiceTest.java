package com.studgrasp.api.domain.roadmapnode;

import com.studgrasp.api.domain.roadmap.Roadmap;
import com.studgrasp.api.domain.roadmap.RoadmapNode;
import com.studgrasp.api.domain.roadmap.RoadmapRepository;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeRequestDTO;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeResponseDTO;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapNodeServiceTest {

    @InjectMocks
    private RoadmapNodeService roadmapNodeService;

    @Mock
    private RoadmapNodeRepository roadmapNodeRepository;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Test
    void shouldCreateNodeSuccessfully() {
        UUID roadmapId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        Roadmap roadmap = Roadmap.builder().id(roadmapId).title("Backend").build();
        RoadmapNodeRequestDTO request = new RoadmapNodeRequestDTO(
                null, "Ponteiros", "Introdução a ponteiros", null, "TOPIC", 1, roadmapId);

        RoadmapNode saved = RoadmapNode.builder()
                .id(nodeId).roadmap(roadmap).title("Ponteiros")
                .description("Introdução a ponteiros").nodeType("TOPIC").nodeOrder(1).build();

        when(roadmapRepository.findById(roadmapId)).thenReturn(Optional.of(roadmap));
        when(roadmapNodeRepository.save(any())).thenReturn(saved);

        RoadmapNodeResponseDTO result = roadmapNodeService.create(request);

        assertNotNull(result);
        assertEquals(nodeId, result.id());
        assertEquals("Ponteiros", result.title());
        assertEquals(roadmapId, result.roadmapId());
        verify(roadmapNodeRepository).save(any());
    }

    @Test
    void shouldThrowWhenRoadmapNotFound() {
        UUID roadmapId = UUID.randomUUID();
        RoadmapNodeRequestDTO request = new RoadmapNodeRequestDTO(
                null, "Node", null, null, "TOPIC", 0, roadmapId);

        when(roadmapRepository.findById(roadmapId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roadmapNodeService.create(request));
        verify(roadmapNodeRepository, never()).save(any());
    }
}
