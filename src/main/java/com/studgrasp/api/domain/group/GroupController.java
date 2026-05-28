package com.studgrasp.api.domain.group;

import com.studgrasp.api.domain.group.dto.*;
import com.studgrasp.api.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDTO> create(
            @RequestBody @Valid GroupRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(dto));
    }

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid MessageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.sendMessage(groupId, user, dto));
    }
}
