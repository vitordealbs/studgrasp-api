package com.studgrasp.api.domain.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "User registration and authentication")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new account and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "User registered successfully, JWT token returned")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login and obtain a JWT token", description = "Authenticates with email and password, returns a signed JWT")
    @ApiResponse(responseCode = "200", description = "Authentication successful, JWT token returned")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
