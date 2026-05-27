package com.studgrasp.api.domain.session;

import com.studgrasp.api.domain.roadmap.RoadmapNode;
import com.studgrasp.api.domain.roadmap.RoadmapNodeRepository;
import com.studgrasp.api.domain.session.dto.StudySessionRequestDTO;
import com.studgrasp.api.domain.session.dto.StudySessionResponseDTO;
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
class StudySessionServiceTest {

    @InjectMocks
    private StudySessionService studySessionService;

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoadmapNodeRepository roadmapNodeRepository;

    @Test
    void shouldStartSessionSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        StudySessionRequestDTO request = new StudySessionRequestDTO(nodeId, now, null, null);

        User user = User.builder().id(userId).build();
        RoadmapNode node = RoadmapNode.builder().id(nodeId).build();
        StudySession saved = StudySession.builder().id(sessionId).user(user).roadmapNode(node).startedAt(now).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roadmapNodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(saved);

        StudySessionResponseDTO response = studySessionService.startSession(userId, request);

        assertNotNull(response);
        assertEquals(sessionId, response.id());
        assertEquals(now, response.startedAt());
        assertNull(response.endedAt());
    }

    @Test
    void shouldEndSessionAndCalculateDuration() {
        UUID sessionId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusMinutes(30);
        LocalDateTime end = start.plusMinutes(30);

        User user = User.builder().id(UUID.randomUUID()).build();
        RoadmapNode node = RoadmapNode.builder().id(UUID.randomUUID()).build();
        StudySession existing = StudySession.builder().id(sessionId).user(user).roadmapNode(node).startedAt(start).build();

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(existing));
        when(studySessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudySessionResponseDTO response = studySessionService.endSession(sessionId, end);

        assertNotNull(response);
        assertEquals(end, response.endedAt());
        assertEquals(1800, response.durationS());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenSessionDoesNotExistOnEnd() {
        UUID sessionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> studySessionService.endSession(sessionId, now));
    }
}