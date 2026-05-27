package com.studgrasp.api.domain.classroom;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassroomResponse(
        UUID id,
        String name,
        String description,
        String advisorName,
        String inviteCode,
        int memberCount,
        LocalDateTime createdAt
) {}