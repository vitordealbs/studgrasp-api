package com.studgrasp.api.domain.progress;

import com.studgrasp.api.domain.progress.dto.UserProgressRequestDTO;
import com.studgrasp.api.domain.progress.dto.UserProgressResponseDTO;
import com.studgrasp.api.domain.roadmap.RoadmapNodeRepository;
import com.studgrasp.api.domain.user.UserRepository;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;

    @Transactional
    public UserProgressResponseDTO updateProgress(UUID userId, UserProgressRequestDTO dto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var node = roadmapNodeRepository.findById(dto.nodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap node not found"));

        var progress = userProgressRepository.findByUserIdAndRoadmapNodeId(userId, dto.nodeId())
                .orElseGet(() -> UserProgress.builder()
                        .user(user)
                        .roadmapNode(node)
                        .build());

        progress.setStatus(dto.status());
        var saved = userProgressRepository.save(progress);

        return new UserProgressResponseDTO(
                saved.getId(),
                saved.getUser().getId(),
                saved.getRoadmapNode().getId(),
                saved.getStatus(),
                saved.getUpdatedAt()
        );
    }
}