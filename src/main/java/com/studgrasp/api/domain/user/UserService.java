package com.studgrasp.api.domain.user;

import com.studgrasp.api.domain.user.dto.AvatarResponse;
import com.studgrasp.api.domain.user.dto.UpdateUserNameRequest;
import com.studgrasp.api.domain.user.dto.UserResponse;
import com.studgrasp.api.infra.exception.ResourceNotFoundException;
import com.studgrasp.api.infra.security.OwnershipValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OwnershipValidator ownershipValidator;

    @Value("${app.avatar.upload-dir:./avatars}")
    private String uploadDir;

    @Transactional
    public UserResponse updateName(UUID id, User principal, UpdateUserNameRequest request) {
        ownershipValidator.requireOwner(principal, id);
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(request.name());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AvatarResponse uploadAvatar(UUID id, User principal, MultipartFile file,
                                       HttpServletRequest httpRequest) {
        ownershipValidator.requireOwnerOrAdvisor(principal, id);
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new IllegalArgumentException("Only JPEG and PNG files are allowed");
        }

        String ext = "image/png".equals(contentType) ? "png" : "jpg";
        String filename = id + "." + ext;

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Files.deleteIfExists(dir.resolve(id + ".jpg"));
            Files.deleteIfExists(dir.resolve(id + ".png"));
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store avatar", e);
        }

        user.setAvatarPath(filename);
        userRepository.save(user);

        String baseUrl = buildBaseUrl(httpRequest);
        return new AvatarResponse(baseUrl + "/api/v1/users/" + id + "/avatar");
    }

    public Resource getAvatarResource(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getAvatarPath() == null) {
            throw new ResourceNotFoundException("Avatar not found");
        }
        Path filePath = Paths.get(uploadDir).resolve(user.getAvatarPath());
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Avatar not found");
        }
        return resource;
    }

    public String getAvatarContentType(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getAvatarPath() == null) {
            throw new ResourceNotFoundException("Avatar not found");
        }
        return user.getAvatarPath().endsWith(".png") ? "image/png" : "image/jpeg";
    }

    private String buildBaseUrl(HttpServletRequest request) {
        int port = request.getServerPort();
        boolean isDefaultPort = (port == 80 && "http".equals(request.getScheme()))
                || (port == 443 && "https".equals(request.getScheme()));
        return request.getScheme() + "://" + request.getServerName()
                + (isDefaultPort ? "" : ":" + port);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
