package com.studgrasp.api.domain.progress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    List<UserProgress> findByUserId(UUID userId);
    Optional<UserProgress> findByUserIdAndRoadmapNodeId(UUID userId, UUID nodeId);

    @Query("SELECT COUNT(p) FROM UserProgress p WHERE p.user.id = :userId AND p.roadmapNode.roadmap.id = :roadmapId AND p.status = 'COMPLETED'")
    long countCompletedByUserAndRoadmap(@Param("userId") UUID userId, @Param("roadmapId") UUID roadmapId);
}