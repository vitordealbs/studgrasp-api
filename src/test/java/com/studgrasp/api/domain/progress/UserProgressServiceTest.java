package com.studgrasp.api.domain.progress;

import com.studgrasp.api.domain.progress.dto.UserProgressRequestDTO;
import com.studgrasp.api.domain.progress.dto.UserProgressResponseDTO;
import com.studgrasp.api.domain.roadmap.RoadmapNode;
import com.studgrasp.api.domain.roadmap.RoadmapNodeRepository;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProgressServiceTest {

    @InjectMocks
    private UserProgressService userProgressService;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoadmapNodeRepository roadmapNodeRepository;

    @Test
    void shouldCreateNewProgressWhenItDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UUID progressId = UUID.randomUUID();
        UserProgressRequestDTO request = new UserProgressRequestDTO(nodeId, "IN_PROGRESS");

        User user = User.builder().id(userId).name("John Doe").build();
        RoadmapNode node = RoadmapNode.builder().id(nodeId).title("Spring Boot Introduction").build();
        UserProgress saved = UserProgress.builder().id(progressId).user(user).roadmapNode(node).status("IN_PROGRESS").updatedAt(LocalDateTime.now()).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roadmapNodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(userProgressRepository.findByUserIdAndRoadmapNodeId(userId, nodeId)).thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(saved);

        UserProgressResponseDTO response = userProgressService.updateProgress(userId, request);

        assertNotNull(response);
        assertEquals(progressId, response.id());
        assertEquals(userId, response.userId());
        assertEquals(nodeId, response.nodeId());
        assertEquals("IN_PROGRESS", response.status());

        verify(userProgressRepository, times(1)).save(any(UserProgress.class));
    }

    @Test
    void shouldUpdateExistingProgressWhenItAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UserProgressRequestDTO request = new UserProgressRequestDTO(nodeId, "DONE");

        User user = User.builder().id(userId).build();
        RoadmapNode node = RoadmapNode.builder().id(nodeId).build();
        UserProgress existing = UserProgress.builder().id(UUID.randomUUID()).user(user).roadmapNode(node).status("IN_PROGRESS").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roadmapNodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(userProgressRepository.findByUserIdAndRoadmapNodeId(userId, nodeId)).thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProgressResponseDTO response = userProgressService.updateProgress(userId, request);

        assertNotNull(response);
        assertEquals("DONE", response.status());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UserProgressRequestDTO request = new UserProgressRequestDTO(UUID.randomUUID(), "DONE");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userProgressService.updateProgress(userId, request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenRoadmapNodeDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UserProgressRequestDTO request = new UserProgressRequestDTO(nodeId, "DONE");

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(roadmapNodeRepository.findById(nodeId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userProgressService.updateProgress(userId, request));
    }
}