package com.studgrasp.api.domain.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, ClassMemberId> {
    List<ClassMember> findByClassroomId(UUID classroomId);
    List<ClassMember> findByUserId(UUID userId);
    boolean existsByClassroomIdAndUserId(UUID classroomId, UUID userId);
}