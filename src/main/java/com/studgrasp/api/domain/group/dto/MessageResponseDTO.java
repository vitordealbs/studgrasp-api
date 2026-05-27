package com.studgrasp.api.domain.group.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponseDTO(
        UUID id,
        UUID groupId,
        UUID senderId,
        String senderName,
        String content,
        LocalDateTime createdAt
) {}