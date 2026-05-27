package com.studgrasp.api.domain.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
    List<Classroom> findByAdvisorId(UUID advisorId);
    Optional<Classroom> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
}