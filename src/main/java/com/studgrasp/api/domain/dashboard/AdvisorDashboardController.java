package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.dashboard.dto.AdvisorDashboardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/advisor")
@RequiredArgsConstructor
public class AdvisorDashboardController {

    private final AdvisorDashboardService service;

    @GetMapping("/{classId}")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<AdvisorDashboardResponseDTO> getDashboard(@PathVariable UUID classId) {
        return ResponseEntity.ok(service.getDashboard(classId));
    }
}
