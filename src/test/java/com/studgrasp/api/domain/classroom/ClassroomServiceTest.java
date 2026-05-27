package com.studgrasp.api.domain.classroom;

import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRole;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    @Mock private ClassroomRepository classroomRepository;
    @Mock private ClassMemberRepository classMemberRepository;

    @InjectMocks
    private ClassroomService classroomService;

    private User advisor;
    private User student;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        advisor = User.builder()
                .id(UUID.randomUUID())
                .name("Prof. Ana")
                .email("ana@studgrasp.com")
                .role(UserRole.ADVISOR)
                .build();

        student = User.builder()
                .id(UUID.randomUUID())
                .name("Vitor Santos")
                .email("vitor@studgrasp.com")
                .role(UserRole.STUDENT)
                .build();

        classroom = Classroom.builder()
                .id(UUID.randomUUID())
                .name("Software Engineering")
                .description("Class 2025")
                .advisor(advisor)
                .inviteCode("ABC12345")
                .build();
    }

    @Test
    void shouldCreateClassroom() {
        var request = new ClassroomRequest("Software Engineering", "Turma 2025");

        when(classroomRepository.existsByInviteCode(any())).thenReturn(false);
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(i -> i.getArgument(0));

        ClassroomResponse response = classroomService.create(request, advisor);

        assertThat(response.name()).isEqualTo("Software Engineering");
        assertThat(response.advisorName()).isEqualTo("Prof. Ana");
        assertThat(response.inviteCode()).isNotNull();
        assertThat(response.inviteCode()).hasSize(8);
        verify(classroomRepository).save(any(Classroom.class));
    }

    @Test
    void shouldJoinClassroomByInviteCode() {
        when(classroomRepository.findByInviteCode("ABC12345")).thenReturn(Optional.of(classroom));
        when(classMemberRepository.existsByClassroomIdAndUserId(classroom.getId(), student.getId()))
                .thenReturn(false);
        when(classMemberRepository.save(any(ClassMember.class))).thenAnswer(i -> i.getArgument(0));
        when(classMemberRepository.findByClassroomId(classroom.getId())).thenReturn(List.of());

        ClassroomResponse response = classroomService.joinByInviteCode("ABC12345", student);

        assertThat(response.name()).isEqualTo("Software Engineering");
        verify(classMemberRepository).save(any(ClassMember.class));
    }

    @Test
    void shouldThrowWhenInviteCodeNotFound() {
        when(classroomRepository.findByInviteCode("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classroomService.joinByInviteCode("invalid", student))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Classroom not found");
    }

    @Test
    void shouldThrowWhenAlreadyMember() {
        when(classroomRepository.findByInviteCode("ABC12345")).thenReturn(Optional.of(classroom));
        when(classMemberRepository.existsByClassroomIdAndUserId(classroom.getId(), student.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> classroomService.joinByInviteCode("ABC12345", student))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You're already member of this Classroom");

        verify(classMemberRepository, never()).save(any());
    }

    @Test
    void shouldListMyClassrooms() {
        var memberEntry = ClassMember.builder()
                .classroom(classroom)
                .user(student)
                .build();

        when(classroomRepository.findByAdvisorId(student.getId())).thenReturn(List.of());
        when(classMemberRepository.findByUserId(student.getId())).thenReturn(List.of(memberEntry));
        when(classMemberRepository.findByClassroomId(classroom.getId())).thenReturn(List.of(memberEntry));

        List<ClassroomResponse> result = classroomService.listMyClassrooms(student);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Software Engineering");
    }

    @Test
    void shouldGetClassroomByIdForMember() {
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(classMemberRepository.existsByClassroomIdAndUserId(classroom.getId(), student.getId()))
                .thenReturn(true);
        when(classMemberRepository.findByClassroomId(classroom.getId())).thenReturn(List.of());

        ClassroomResponse response = classroomService.getById(classroom.getId(), student);

        assertThat(response.id()).isEqualTo(classroom.getId());
        assertThat(response.name()).isEqualTo("Software Engineering");
    }

    @Test
    void shouldThrowWhenUserHasNoAccess() {
        var stranger = User.builder()
                .id(UUID.randomUUID())
                .email("another@test.com")
                .role(UserRole.STUDENT)
                .build();

        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(classMemberRepository.existsByClassroomIdAndUserId(classroom.getId(), stranger.getId()))
                .thenReturn(false);

        assertThatThrownBy(() -> classroomService.getById(classroom.getId(), stranger))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldThrowWhenClassroomNotFound() {
        UUID fakeId = UUID.randomUUID();
        when(classroomRepository.findById(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classroomService.getById(fakeId, student))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}