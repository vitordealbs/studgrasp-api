package com.studgrasp.api.domain.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapCoauthorRepository extends JpaRepository<RoadmapCoauthor, UUID> {

    boolean existsByRoadmapIdAndUserId(UUID roadmapId, UUID userId);

    List<RoadmapCoauthor> findByRoadmapId(UUID roadmapId);

    void deleteByRoadmapIdAndUserId(UUID roadmapId, UUID userId);

    @Query("SELECT rc.user.id FROM RoadmapCoauthor rc WHERE rc.roadmap.id = :roadmapId")
    List<UUID> findCoauthorUserIdsByRoadmapId(@Param("roadmapId") UUID roadmapId);
}
