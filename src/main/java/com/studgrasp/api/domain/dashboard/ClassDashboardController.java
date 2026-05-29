package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.classroom.ClassroomRepository;
import com.studgrasp.api.domain.dashboard.dto.ClassDashboardResponseDTO;
import com.studgrasp.api.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class ClassDashboardController {

    private final ClassDashboardService service;
    private final ClassroomRepository classroomRepository;

    @GetMapping("/class/{classId}")
    public ResponseEntity<ClassDashboardResponseDTO> getClassDashboard(
            @PathVariable UUID classId,
            @AuthenticationPrincipal User user) {

        var classroom = classroomRepository.findByIdWithAdvisor(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));

        if (!classroom.getAdvisor().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the class advisor can access this dashboard");
        }

        return ResponseEntity.ok(service.getDashboard(classId));
    }
}
