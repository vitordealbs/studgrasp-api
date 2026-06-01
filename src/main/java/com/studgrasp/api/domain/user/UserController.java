package com.studgrasp.api.domain.user;

import com.studgrasp.api.config.OpenApiConfig;
import com.studgrasp.api.domain.user.dto.AvatarResponse;
import com.studgrasp.api.domain.user.dto.UpdateUserNameRequest;
import com.studgrasp.api.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Users", description = "User profile management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Update display name", description = "Updates the authenticated user's display name. Only the owner may change their own name.")
    @ApiResponse(responseCode = "200", description = "Name updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Token user does not match path id")
    @ApiResponse(responseCode = "404", description = "User not found")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateName(
            @PathVariable UUID id,
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid UpdateUserNameRequest request) {
        return ResponseEntity.ok(userService.updateName(id, principal, request));
    }

    @Operation(summary = "Upload avatar", description = "Uploads a profile avatar (JPEG or PNG, max 2 MB). Overwrites any previous avatar.")
    @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Missing file, wrong MIME type, or file too large")
    @ApiResponse(responseCode = "403", description = "Token user does not match path id")
    @ApiResponse(responseCode = "404", description = "User not found")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarResponse> uploadAvatar(
            @PathVariable UUID id,
            @AuthenticationPrincipal User principal,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        return ResponseEntity.ok(userService.uploadAvatar(id, principal, file, request));
    }

    @Operation(summary = "Get avatar (redirect)", description = "Redirects to the avatar image. Returns 404 if no avatar is set.")
    @ApiResponse(responseCode = "302", description = "Redirect to avatar image URL")
    @ApiResponse(responseCode = "404", description = "No avatar set for this user")
    @GetMapping("/{id}/avatar")
    public ResponseEntity<Void> redirectAvatar(@PathVariable UUID id) {
        userService.getAvatarResource(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/api/v1/users/" + id + "/avatar/file"))
                .build();
    }

    @Operation(summary = "Serve avatar image", description = "Streams the avatar image. Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Avatar image")
    @ApiResponse(responseCode = "404", description = "No avatar set for this user")
    @GetMapping("/{id}/avatar/file")
    public ResponseEntity<Resource> serveAvatar(@PathVariable UUID id) {
        Resource resource = userService.getAvatarResource(id);
        String contentType = userService.getAvatarContentType(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
