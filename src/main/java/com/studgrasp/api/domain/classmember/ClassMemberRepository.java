package com.studgrasp.api.domain.classmember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, ClassMemberId> {
    List<ClassMember> findByClassroomId(UUID classroomId);
    long countByClassroomId(UUID classroomId);
    List<ClassMember> findByUserId(UUID userId);
    boolean existsByClassroomIdAndUserId(UUID classroomId, UUID userId);

    @Query("SELECT cm.classroom.id, COUNT(cm) FROM ClassMember cm " +
           "WHERE cm.classroom.id IN :classroomIds " +
           "GROUP BY cm.classroom.id")
    List<Object[]> countMembersGroupByClassroomId(@Param("classroomIds") List<UUID> classroomIds);
}
