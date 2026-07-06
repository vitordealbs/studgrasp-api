package com.studgrasp.api.domain.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {
    List<Roadmap> findByCareerType(String careerType);
    Optional<Roadmap> findByCareerTypeIgnoreCase(String careerType);
    List<Roadmap> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);
    List<Roadmap> findByIsPublicTrueOrderByCreatedAtDesc();
}
