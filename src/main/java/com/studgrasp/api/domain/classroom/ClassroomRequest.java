package com.studgrasp.api.domain.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClassroomRequest(
        @NotBlank(message = "You must register your name")
        @Size(max = 100)
        String name,

        String description
) {}