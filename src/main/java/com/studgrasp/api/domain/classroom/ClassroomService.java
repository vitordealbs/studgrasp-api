package com.studgrasp.api.domain.classroom;

import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassroomService {

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
        var classroom = classroomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        if (classMemberRepository.existsByClassroomIdAndUserId(classroom.getId(), user.getId())) {
            throw new IllegalArgumentException("You're already member of this Classroom");
        }

        var member = ClassMember.builder()
                .classroom(classroom)
                .user(user)
                .build();

        classMemberRepository.save(member);

        int count = classMemberRepository.findByClassroomId(classroom.getId()).size();
        return toResponse(classroom, count);
    }

    public List<ClassroomResponse> listMyClassrooms(User user) {
        var asAdvisor = classroomRepository.findByAdvisorId(user.getId())
                .stream().map(c -> {
                    int count = classMemberRepository.findByClassroomId(c.getId()).size();
                    return toResponse(c, count);
                }).toList();

        var asMember = classMemberRepository.findByUserId(user.getId())
                .stream().map(m -> {
                    int count = classMemberRepository.findByClassroomId(m.getClassroom().getId()).size();
                    return toResponse(m.getClassroom(), count);
                }).toList();

        return java.util.stream.Stream.concat(asAdvisor.stream(), asMember.stream()).toList();
    }

    public ClassroomResponse getById(UUID id, User user) {
        var classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("classroom", id.toString()));

        boolean isAdvisor = classroom.getAdvisor().getId().equals(user.getId());
        boolean isMember = classMemberRepository.existsByClassroomIdAndUserId(id, user.getId());

        if (!isAdvisor && !isMember) {
            throw new AccessDeniedException("You don't have access to this class");
        }

        int count = classMemberRepository.findByClassroomId(id).size();
        return toResponse(classroom, count);
    }

    private String generateInviteCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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