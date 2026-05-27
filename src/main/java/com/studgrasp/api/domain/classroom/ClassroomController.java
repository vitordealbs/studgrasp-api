package com.studgrasp.api.domain.classroom;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.studgrasp.api.domain.user.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    public ResponseEntity<ClassroomResponse> create(
            @RequestBody @Valid ClassroomRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classroomService.create(request, user));
    }

    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<ClassroomResponse> join(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.joinByInviteCode(inviteCode, user));
    }

    @GetMapping
    public ResponseEntity<List<ClassroomResponse>> listMine(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.listMyClassrooms(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.getById(id, user));
    }
}