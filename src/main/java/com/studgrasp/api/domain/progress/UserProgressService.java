package com.studgrasp.api.domain.progress;

import com.studgrasp.api.domain.progress.dto.RoadmapProgressSummaryDTO;
import com.studgrasp.api.domain.progress.dto.UserProgressRequestDTO;
import com.studgrasp.api.domain.progress.dto.UserProgressResponseDTO;
import com.studgrasp.api.domain.roadmap.RoadmapRepository;
import com.studgrasp.api.domain.roadmapnode.RoadmapNodeRepository;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final RoadmapRepository roadmapRepository;

    @Transactional
    public UserProgressResponseDTO updateProgress(User user, UserProgressRequestDTO dto) {
        var node = roadmapNodeRepository.findById(dto.nodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap node not found"));

        var progress = userProgressRepository.findByUserIdAndRoadmapNodeId(user.getId(), dto.nodeId())
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

    public RoadmapProgressSummaryDTO getRoadmapProgressSummary(UUID roadmapId, User user) {
        if (!roadmapRepository.existsById(roadmapId)) {
            throw new ResourceNotFoundException("Roadmap not found");
        }
        long total = roadmapNodeRepository.countByRoadmapId(roadmapId);
        long completed = userProgressRepository.countCompletedByUserAndRoadmap(user.getId(), roadmapId);
        return new RoadmapProgressSummaryDTO(roadmapId, (int) total, (int) completed);
    }
}
