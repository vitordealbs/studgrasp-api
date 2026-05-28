package com.studgrasp.api.domain.group;

import com.studgrasp.api.domain.classroom.ClassroomRepository;
import com.studgrasp.api.domain.group.dto.*;
import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;
    private final ClassroomRepository classroomRepository;

    @Transactional
    public GroupResponseDTO createGroup(GroupRequestDTO dto) {
        var classroom = classroomRepository.findById(dto.classroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        var group = Group.builder()
                .name(dto.name())
                .classroom(classroom)
                .build();

        var savedGroup = groupRepository.save(group);
        return new GroupResponseDTO(savedGroup.getId(), savedGroup.getName(), classroom.getId());
    }

    @Transactional
    public MessageResponseDTO sendMessage(UUID groupId, User sender, MessageRequestDTO dto) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        var message = Message.builder()
                .group(group)
                .sender(sender)
                .content(dto.content())
                .build();

        var savedMessage = messageRepository.save(message);

        return new MessageResponseDTO(
                savedMessage.getId(),
                group.getId(),
                sender.getId(),
                sender.getName(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt()
        );
    }
}
