package com.studgrasp.api.domain.classroom;

import com.studgrasp.api.domain.classmember.ClassMember;
import com.studgrasp.api.domain.classmember.ClassMemberRepository;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private static final int INVITE_CODE_LENGTH = 8;

    private final ClassroomRepository classroomRepository;
    private final ClassMemberRepository classMemberRepository;

    @Transactional
    public ClassroomResponse create(ClassroomRequest request, User advisor) {
        var classroom = Classroom.builder()
                .name(request.name())
                .description(request.description())
                .advisor(advisor)
                .inviteCode(generateInviteCode())
                .build();

        classroomRepository.save(classroom);
        return toResponse(classroom, 0);
    }

    @Transactional
    public ClassroomResponse joinByInviteCode(String inviteCode, User user) {
        var classroom = classroomRepository.findByInviteCodeWithAdvisor(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        if (classMemberRepository.existsByClassroomIdAndUserId(classroom.getId(), user.getId())) {
            throw new IllegalArgumentException("You're already member of this Classroom");
        }

        var member = ClassMember.builder()
                .classroom(classroom)
                .user(user)
                .build();

        classMemberRepository.save(member);

        int count = (int) classMemberRepository.countByClassroomId(classroom.getId());
        return toResponse(classroom, count);
    }

    public List<ClassroomResponse> listMyClassrooms(User user) {
        List<Classroom> classrooms = classroomRepository.findAllAccessibleByUserId(user.getId());
        if (classrooms.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = classrooms.stream().map(Classroom::getId).toList();
        Map<UUID, Long> countByClassroom = classMemberRepository
                .countMembersGroupByClassroomId(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));

        return classrooms.stream()
                .map(c -> toResponse(c, countByClassroom.getOrDefault(c.getId(), 0L).intValue()))
                .toList();
    }

    public ClassroomResponse getById(UUID id, User user) {
        var classroom = classroomRepository.findByIdWithAdvisor(id)
                .orElseThrow(() -> new ResourceNotFoundException("classroom", id.toString()));

        boolean isAdvisor = classroom.getAdvisor().getId().equals(user.getId());
        boolean isMember = classMemberRepository.existsByClassroomIdAndUserId(id, user.getId());

        if (!isAdvisor && !isMember) {
            throw new AccessDeniedException("You don't have access to this class");
        }

        int count = (int) classMemberRepository.countByClassroomId(id);
        return toResponse(classroom, count);
    }

    private String generateInviteCode() {
        String code;
        do {
            code = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, INVITE_CODE_LENGTH)
                    .toUpperCase();
        } while (classroomRepository.existsByInviteCode(code));
        return code;
    }

    private ClassroomResponse toResponse(Classroom c, int memberCount) {
        return new ClassroomResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getAdvisor().getName(),
                c.getInviteCode(),
                memberCount,
                c.getCreatedAt()
        );
    }
}
