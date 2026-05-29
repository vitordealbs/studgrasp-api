package com.studgrasp.api.domain.group;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.group.dto.*;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Groups", description = "Study groups and group messaging")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "Create a study group", description = "Creates a new study group that can be used for collaborative learning")
    @ApiResponse(responseCode = "201", description = "Group created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping
    public ResponseEntity<GroupResponseDTO> create(
            @RequestBody @Valid GroupRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(dto));
    }

    @Operation(summary = "Send a message to a group", description = "Posts a message from the authenticated user to the specified study group")
    @ApiResponse(responseCode = "201", description = "Message sent successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @PostMapping("/{groupId}/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @Parameter(description = "UUID of the study group") @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid MessageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.sendMessage(groupId, user, dto));
    }
}
