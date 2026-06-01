package com.studgrasp.api.infra.security;

import com.studgrasp.api.domain.user.User;
import com.studgrasp.api.domain.user.UserRole;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validates that the currently authenticated user either owns the requested resource
 * (their ID matches the path parameter) or holds the ADVISOR role.
 */
@Component
public class OwnershipValidator {

    /**
     * Throws {@link AuthorizationDeniedException} (HTTP 403) unless the principal
     * is the exact owner of the resource (no role bypass).
     */
    public void requireOwner(User principal, UUID resourceUserId) {
        if (principal == null) {
            throw new AuthorizationDeniedException("Access Denied", () -> false);
        }
        if (!resourceUserId.equals(principal.getId())) {
            throw new AuthorizationDeniedException("Access Denied", () -> false);
        }
    }

    /**
     * Throws {@link AuthorizationDeniedException} (HTTP 403) unless the principal
     * is the owner of the resource or has the ADVISOR role.
     */
    public void requireOwnerOrAdvisor(User principal, UUID resourceUserId) {
        if (principal == null) {
            throw new AuthorizationDeniedException("Access Denied",
                    () -> false);
        }
        boolean isOwner = resourceUserId.equals(principal.getId());
        boolean isAdvisor = principal.getRole() == UserRole.ADVISOR;
        if (!isOwner && !isAdvisor) {
            throw new AuthorizationDeniedException("Access Denied",
                    () -> false);
        }
    }
}
