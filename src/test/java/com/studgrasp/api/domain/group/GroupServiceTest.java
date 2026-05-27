package com.studgrasp.api.domain.group;

import com.studgrasp.api.domain.classroom.Classroom;
import com.studgrasp.api.domain.classroom.ClassroomRepository;
import com.studgrasp.api.domain.group.dto.*;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRepository;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldCreateGroupWhenClassroomExists() {
        UUID classroomId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        GroupRequestDTO requestDTO = new GroupRequestDTO("Backend Study Group", classroomId);

        Classroom classroom = Classroom.builder().id(classroomId).name("Computer Science 2026.1").build();
        Group savedGroup = Group.builder().id(groupId).name(requestDTO.name()).classroom(classroom).build();

        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        GroupResponseDTO response = groupService.createGroup(requestDTO);

        assertNotNull(response);
        assertEquals(groupId, response.id());
        assertEquals("Backend Study Group", response.name());
        assertEquals(classroomId, response.classroomId());

        verify(classroomRepository, times(1)).findById(classroomId);
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenClassroomDoesNotExist() {
        UUID classroomId = UUID.randomUUID();
        GroupRequestDTO requestDTO = new GroupRequestDTO("Ghost Group", classroomId);

        when(classroomRepository.findById(classroomId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.createGroup(requestDTO));

        verify(classroomRepository, times(1)).findById(classroomId);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void shouldSendMessageWhenGroupAndUserExist() {
        UUID groupId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        MessageRequestDTO requestDTO = new MessageRequestDTO("Hello everyone, testing the chat!", senderId);

        Group group = Group.builder().id(groupId).name("AI Group").build();
        User sender = User.builder().id(senderId).name("Vitor Albano").build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));

        UUID messageId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(messageId);
            msg.setCreatedAt(now);
            return msg;
        });

        MessageResponseDTO response = groupService.sendMessage(groupId, requestDTO);

        assertNotNull(response);
        assertEquals(messageId, response.id());
        assertEquals(groupId, response.groupId());
        assertEquals(senderId, response.senderId());
        assertEquals("Vitor Albano", response.senderName());
        assertEquals(requestDTO.content(), response.content());
        assertEquals(now, response.createdAt());

        verify(groupRepository, times(1)).findById(groupId);
        verify(userRepository, times(1)).findById(senderId);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        MessageRequestDTO requestDTO = new MessageRequestDTO("Lost message", UUID.randomUUID());

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.sendMessage(groupId, requestDTO));

        verify(groupRepository, times(1)).findById(groupId);
        verify(userRepository, never()).findById(any());
        verify(messageRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        MessageRequestDTO requestDTO = new MessageRequestDTO("Message from a ghost", senderId);

        Group group = Group.builder().id(groupId).name("General Group").build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.sendMessage(groupId, requestDTO));

        verify(groupRepository, times(1)).findById(groupId);
        verify(userRepository, times(1)).findById(senderId);
        verify(messageRepository, never()).save(any());
    }
}