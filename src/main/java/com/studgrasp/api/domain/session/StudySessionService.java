package com.studgrasp.api.domain.session;

import com.studgrasp.api.domain.roadmap.RoadmapNodeRepository;
import com.studgrasp.api.domain.session.dto.StudySessionRequestDTO;
import com.studgrasp.api.domain.session.dto.StudySessionResponseDTO;
import com.studgrasp.api.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserRepository userRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;

    @Transactional
    public StudySessionResponseDTO startSession(UUID userId, StudySessionRequestDTO dto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var node = roadmapNodeRepository.findById(dto.nodeId())
                .orElseThrow(() -> new EntityNotFoundException("Roadmap node not found"));

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
    public StudySessionResponseDTO endSession(UUID sessionId, LocalDateTime endedAt) {
        var session = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Study session not found"));

        session.setEndedAt(endedAt);

        long seconds = Duration.between(session.getStartedAt(), endedAt).toSeconds();
        session.setDurationS((int) seconds);

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