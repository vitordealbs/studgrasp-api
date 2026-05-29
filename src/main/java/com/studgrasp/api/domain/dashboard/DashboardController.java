package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.dashboard.dto.DashboardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/{userId}")
    public ResponseEntity<DashboardResponseDTO> getDashboard(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getDashboard(userId));
    }
}
