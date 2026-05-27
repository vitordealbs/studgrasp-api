package com.studgrasp.api.infra.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Map<String, String> fields,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String message, Map<String, String> fields) {
        this(status, message, fields, LocalDateTime.now());
    }
}