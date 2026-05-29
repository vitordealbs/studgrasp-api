package com.studgrasp.api.domain.classroom;

import com.studgrasp.api.config.OpenApiConfig;
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

    @Operation(summary = "Create a classroom (ADVISOR only)", description = "Creates a new classroom owned by the authenticated advisor and generates an invite code")
    @ApiResponse(responseCode = "201", description = "Classroom created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Requires ADVISOR role")
    @PostMapping
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<ClassroomResponse> create(
            @RequestBody @Valid ClassroomRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classroomService.create(request, user));
    }

    @Operation(summary = "Join a classroom by invite code", description = "Enrolls the authenticated user in the classroom associated with the provided invite code")
    @ApiResponse(responseCode = "200", description = "Joined classroom successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Invite code not found")
    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<ClassroomResponse> join(
            @Parameter(description = "Unique classroom invite code") @PathVariable String inviteCode,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.joinByInviteCode(inviteCode, user));
    }

    @Operation(summary = "List classrooms for the authenticated user", description = "Returns all classrooms the user belongs to, either as advisor or student")
    @ApiResponse(responseCode = "200", description = "Classrooms listed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping
    public ResponseEntity<List<ClassroomResponse>> listMine(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.listMyClassrooms(user));
    }

    @Operation(summary = "Get classroom by ID", description = "Returns classroom details; only accessible to members of the classroom")
    @ApiResponse(responseCode = "200", description = "Classroom found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "User is not a member of this classroom")
    @ApiResponse(responseCode = "404", description = "Classroom not found")
    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getById(
            @Parameter(description = "Classroom UUID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.getById(id, user));
    }
}
