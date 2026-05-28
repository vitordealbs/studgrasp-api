package com.studgrasp.api.domain.classroom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
    List<Classroom> findByAdvisorId(UUID advisorId);
    Optional<Classroom> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);

    @Query("SELECT DISTINCT c FROM Classroom c " +
           "LEFT JOIN ClassMember cm ON cm.classroom = c " +
           "WHERE c.advisor.id = :userId OR cm.user.id = :userId")
    List<Classroom> findAllAccessibleByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM Classroom c ORDER BY c.createdAt DESC")
    Page<Classroom> findAllPaged(Pageable pageable);
}
