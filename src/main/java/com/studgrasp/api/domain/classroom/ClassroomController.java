package com.studgrasp.api.domain.classroom;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Classrooms", description = "Classroom management")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @Operation(summary = "Create a classroom (ADVISOR only)")
    @PostMapping
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<ClassroomResponse> create(
            @RequestBody @Valid ClassroomRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classroomService.create(request, user));
    }

    @Operation(summary = "Join a classroom by invite code")
    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<ClassroomResponse> join(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.joinByInviteCode(inviteCode, user));
    }

    @Operation(summary = "List classrooms for the authenticated user")
    @GetMapping
    public ResponseEntity<List<ClassroomResponse>> listMine(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.listMyClassrooms(user));
    }

    @Operation(summary = "Get classroom by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.getById(id, user));
    }
}
