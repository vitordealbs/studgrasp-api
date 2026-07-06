package com.studgrasp.api.domain.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "You must write your Name")
        String name,

        @NotBlank(message = "You must write your E-mail")
        @Email(message = "Invalid e-mail")
        String email,

        @NotBlank(message = "You must write your Password")
        @Size(min = 12, message = "Password must be at least 12 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[A-Za-z\\d@$!%*?&#+\\-=\\[\\]{};':\"\\\\|,.<>\\/?^()_]{12,}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character"
        )
        String password,

        String role,

        @NotNull(message = "You must accept the Terms of Service and Privacy Policy")
        @AssertTrue(message = "You must accept the Terms of Service and Privacy Policy")
        Boolean termsAccepted
) {}