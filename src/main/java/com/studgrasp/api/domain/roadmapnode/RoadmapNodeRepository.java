package com.studgrasp.api.domain.roadmapnode;

import com.studgrasp.api.domain.roadmap.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, UUID> {
    List<RoadmapNode> findByRoadmapIdOrderByNodeOrderAsc(UUID roadmapId);

    @Query("SELECT COUNT(n) FROM RoadmapNode n WHERE n.roadmap.id = :roadmapId")
    long countByRoadmapId(@Param("roadmapId") UUID roadmapId);
}