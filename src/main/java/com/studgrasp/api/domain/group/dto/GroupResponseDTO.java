package com.studgrasp.api.domain.group.dto;

import java.util.UUID;

public record GroupResponseDTO(
        UUID id,
        String name,
        UUID classroomId
) {}