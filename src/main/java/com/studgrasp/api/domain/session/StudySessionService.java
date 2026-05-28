package com.studgrasp.api.domain.session;

import com.studgrasp.api.domain.roadmapnode.RoadmapNodeRepository;
import com.studgrasp.api.domain.session.dto.StudySessionRequestDTO;
import com.studgrasp.api.domain.session.dto.StudySessionResponseDTO;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;

    @Transactional
    public StudySessionResponseDTO startSession(User user, StudySessionRequestDTO dto) {
        var node = roadmapNodeRepository.findById(dto.nodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap node not found"));

        var session = StudySession.builder()
                .user(user)
                .roadmapNode(node)
                .startedAt(dto.startedAt())
                .build();

        var saved = studySessionRepository.save(session);

        return new StudySessionResponseDTO(
                saved.getId(),
                saved.getUser().getId(),
                saved.getRoadmapNode().getId(),
                saved.getStartedAt(),
                saved.getEndedAt(),
                saved.getDurationS()
        );
    }

    @Transactional
    public StudySessionResponseDTO endSession(UUID sessionId, User user, LocalDateTime endedAt) {
        var session = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot end another user's session");
        }

        session.setEndedAt(endedAt);
        session.setDurationS((int) Duration.between(session.getStartedAt(), endedAt).toSeconds());

        var saved = studySessionRepository.save(session);

        return new StudySessionResponseDTO(
                saved.getId(),
                saved.getUser().getId(),
                saved.getRoadmapNode().getId(),
                saved.getStartedAt(),
                saved.getEndedAt(),
                saved.getDurationS()
        );
    }
}
