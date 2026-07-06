package com.studgrasp.api.domain.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedRoadmapRepository extends JpaRepository<SavedRoadmap, UUID> {

    Optional<SavedRoadmap> findByUserIdAndRoadmapId(UUID userId, UUID roadmapId);

    boolean existsByUserIdAndRoadmapId(UUID userId, UUID roadmapId);

    @Query("SELECT sr.roadmap FROM SavedRoadmap sr WHERE sr.user.id = :userId")
    List<Roadmap> findRoadmapsByUserId(@Param("userId") UUID userId);

    void deleteByUserIdAndRoadmapId(UUID userId, UUID roadmapId);
}
