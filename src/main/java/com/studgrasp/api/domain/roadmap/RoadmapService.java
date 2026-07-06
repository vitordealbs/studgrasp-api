package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.roadmapnode.RoadmapNodeRepository;
import com.studgrasp.api.domain.roadmapnode.dto.RoadmapNodeResponseDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapRequestDTO;
import com.studgrasp.api.domain.roadmap.dto.RoadmapResponseDTO;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final SavedRoadmapRepository savedRoadmapRepository;
    private final RoadmapCoauthorRepository coauthorRepository;
    private final com.studgrasp.api.domain.user.UserRepository userRepository;

    @Transactional
    public RoadmapResponseDTO create(RoadmapRequestDTO request) {
        var roadmap = Roadmap.builder()
                .title(request.title())
                .careerType(request.careerType().toUpperCase())
                .sourceUrl(request.sourceUrl())
                .isPublic(false)
                .isCustom(false)
                .build();

        roadmapRepository.save(roadmap);
        return toResponse(roadmap, List.of(), null);
    }

    @Transactional
    public RoadmapResponseDTO createCustom(RoadmapRequestDTO request, UUID userId) {
        var roadmap = Roadmap.builder()
                .title(request.title())
                .careerType(request.careerType().toUpperCase())
                .sourceUrl(request.sourceUrl())
                .createdBy(userId)
                .isPublic(request.isPublic())
                .isCustom(true)
                .build();

        roadmapRepository.save(roadmap);
        return toResponse(roadmap, List.of(), userId);
    }

    @Transactional(readOnly = true)
    public Page<RoadmapResponseDTO> getAllRoadmaps(Pageable pageable) {
        return roadmapRepository.findAll(pageable)
                .map(roadmap -> toResponse(roadmap, List.of(), null));
    }

    @Transactional(readOnly = true)
    public List<RoadmapResponseDTO> getMyRoadmaps(UUID userId) {
        // Get roadmaps created by user + saved roadmaps
        List<Roadmap> createdByUser = roadmapRepository.findByCreatedByOrderByCreatedAtDesc(userId);
        List<Roadmap> savedByUser = savedRoadmapRepository.findRoadmapsByUserId(userId);

        List<RoadmapResponseDTO> result = new java.util.ArrayList<>();

        // Add created roadmaps
        createdByUser.forEach(r -> result.add(toResponse(r, buildNodes(r.getId()), userId)));

        // Add saved roadmaps (avoid duplicates)
        savedByUser.stream()
                .filter(r -> !createdByUser.contains(r))
                .forEach(r -> result.add(toResponse(r, buildNodes(r.getId()), userId)));

        return result;
    }

    @Transactional(readOnly = true)
    public List<RoadmapResponseDTO> getPublicRoadmaps(UUID userId) {
        return roadmapRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(roadmap -> toResponse(roadmap, List.of(), userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoadmapResponseDTO getRoadmapWithNodes(UUID roadmapId) {
        return getRoadmapWithNodes(roadmapId, null);
    }

    @Transactional(readOnly = true)
    public RoadmapResponseDTO getRoadmapWithNodes(UUID roadmapId, UUID userId) {
        var roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));

        return toResponse(roadmap, buildNodes(roadmapId), userId);
    }

    @Transactional(readOnly = true)
    public RoadmapResponseDTO getByCareerType(String careerType) {
        var roadmap = roadmapRepository.findByCareerTypeIgnoreCase(careerType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Roadmap not found for career: " + careerType));

        return toResponse(roadmap, buildNodes(roadmap.getId()), null);
    }

    @Transactional
    public void saveRoadmap(UUID roadmapId, UUID userId) {
        var roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));

        if (!roadmap.isPublic()) {
            throw new IllegalArgumentException("Can only save public roadmaps");
        }

        if (savedRoadmapRepository.existsByUserIdAndRoadmapId(userId, roadmapId)) {
            return; // Already saved
        }

        var savedRoadmap = SavedRoadmap.builder()
                .user(new com.studgrasp.api.domain.user.User())
                .roadmap(roadmap)
                .build();
        savedRoadmap.getUser().setId(userId);

        savedRoadmapRepository.save(savedRoadmap);
    }

    @Transactional
    public void unsaveRoadmap(UUID roadmapId, UUID userId) {
        savedRoadmapRepository.deleteByUserIdAndRoadmapId(userId, roadmapId);
    }

    // Coauthor management
    @Transactional
    public void addCoauthor(UUID roadmapId, String coauthorEmail, UUID invitedBy) {
        var roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));

        // Only creator can add coauthors
        if (!roadmap.getCreatedBy().equals(invitedBy)) {
            throw new IllegalArgumentException("Only the roadmap creator can add coauthors");
        }

        var coauthor = userRepository.findByEmail(coauthorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + coauthorEmail));

        // Can't add yourself
        if (coauthor.getId().equals(invitedBy)) {
            throw new IllegalArgumentException("You cannot add yourself as a coauthor");
        }

        // Check if already coauthor
        if (coauthorRepository.existsByRoadmapIdAndUserId(roadmapId, coauthor.getId())) {
            throw new IllegalArgumentException("User is already a coauthor");
        }

        var inviter = new com.studgrasp.api.domain.user.User();
        inviter.setId(invitedBy);

        var coauthorEntity = RoadmapCoauthor.builder()
                .roadmap(roadmap)
                .user(coauthor)
                .invitedBy(inviter)
                .build();

        coauthorRepository.save(coauthorEntity);
    }

    @Transactional
    public void removeCoauthor(UUID roadmapId, UUID coauthorId, UUID requestingUserId) {
        var roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));

        // Only creator can remove coauthors
        if (!roadmap.getCreatedBy().equals(requestingUserId)) {
            throw new IllegalArgumentException("Only the roadmap creator can remove coauthors");
        }

        coauthorRepository.deleteByRoadmapIdAndUserId(roadmapId, coauthorId);
    }

    @Transactional(readOnly = true)
    public List<com.studgrasp.api.domain.roadmap.dto.CoauthorDTO> getCoauthors(UUID roadmapId) {
        return coauthorRepository.findByRoadmapId(roadmapId)
                .stream()
                .map(ca -> new com.studgrasp.api.domain.roadmap.dto.CoauthorDTO(
                        ca.getId(),
                        ca.getUser().getId(),
                        ca.getUser().getName(),
                        ca.getUser().getEmail(),
                        ca.getInvitedBy().getId(),
                        ca.getInvitedAt()
                ))
                .toList();
    }

    @Transactional
    public RoadmapResponseDTO cloneRoadmap(UUID roadmapId, UUID userId) {
        // Find the original roadmap
        var original = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));

        // Create a copy with the user as creator
        var cloned = Roadmap.builder()
                .title(original.getTitle() + " (Cópia)")
                .careerType(original.getCareerType())
                .sourceUrl(original.getSourceUrl())
                .createdBy(userId)
                .isPublic(false)  // Clones are private by default
                .isCustom(true)   // Clones are custom roadmaps
                .build();

        roadmapRepository.save(cloned);

        // Clone all nodes
        var originalNodes = roadmapNodeRepository.findByRoadmapIdOrderByNodeOrderAsc(roadmapId);
        for (var originalNode : originalNodes) {
            var clonedNode = com.studgrasp.api.domain.roadmap.RoadmapNode.builder()
                    .roadmap(cloned)
                    .parentId(originalNode.getParentId())
                    .title(originalNode.getTitle())
                    .description(originalNode.getDescription())
                    .nodeType(originalNode.getNodeType())
                    .nodeOrder(originalNode.getNodeOrder())
                    .build();
            roadmapNodeRepository.save(clonedNode);
        }

        return toResponse(cloned, buildNodes(cloned.getId()), userId);
    }

    public boolean canEditRoadmap(UUID roadmapId, UUID userId) {
        var roadmap = roadmapRepository.findById(roadmapId).orElse(null);
        if (roadmap == null) return false;

        // Creator can edit
        if (roadmap.getCreatedBy() != null && roadmap.getCreatedBy().equals(userId)) {
            return true;
        }

        // Coauthors can edit
        return coauthorRepository.existsByRoadmapIdAndUserId(roadmapId, userId);
    }

    private List<RoadmapNodeResponseDTO> buildNodes(UUID roadmapId) {
        return roadmapNodeRepository.findByRoadmapIdOrderByNodeOrderAsc(roadmapId)
                .stream()
                .map(node -> new RoadmapNodeResponseDTO(
                        node.getId(),
                        node.getTitle(),
                        node.getDescription(),
                        node.getParentId(),
                        node.getNodeType(),
                        node.getNodeOrder(),
                        roadmapId
                )).toList();
    }

    private RoadmapResponseDTO toResponse(Roadmap roadmap, List<RoadmapNodeResponseDTO> nodes, UUID userId) {
        boolean isSaved = userId != null &&
                savedRoadmapRepository.existsByUserIdAndRoadmapId(userId, roadmap.getId());

        return new RoadmapResponseDTO(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getCareerType(),
                roadmap.getSourceUrl(),
                nodes,
                roadmap.getCreatedBy(),
                roadmap.isPublic(),
                roadmap.isCustom(),
                isSaved
        );
    }
}
