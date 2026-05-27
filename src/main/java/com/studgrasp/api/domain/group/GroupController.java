package com.studgrasp.api.domain.group;

import com.studgrasp.api.domain.group.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDTO> create(@RequestBody @Valid GroupRequestDTO dto) {
        var response = groupService.createGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @PathVariable UUID groupId,
            @RequestBody @Valid MessageRequestDTO dto) {
        var response = groupService.sendMessage(groupId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}