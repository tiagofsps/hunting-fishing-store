package com.tiagofsps.hunting_fishing_store.config.security;

import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtil {

    private final UserRepository userRepository;

    public AuthorizationUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks if the authenticated user is authorized to access a resource.
     * Admins have access to all resources, regular users only their own.
     * The decision is ID-based: the authenticated principal's own user id
     * (resolved from the authenticated email) must match the resource owner id.
     *
     * @param requestedResourceOwnerId The ID of the resource owner
     * @param userEmail The email of the resource owner (kept for call compatibility; the decision is ID-based)
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorizedToAccess(Long requestedResourceOwnerId, String userEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || requestedResourceOwnerId == null) {
            return false;
        }

        if (isAdmin()) {
            return true;
        }

        // The authenticated principal's name is the user's email.
        User authenticatedUser = userRepository.findByEmail(authentication.getName()).orElse(null);
        return authenticatedUser != null && authenticatedUser.getId().equals(requestedResourceOwnerId);
    }

    /**
     * Checks if the authenticated user is an admin.
     *
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Gets the authenticated user's email/username.
     *
     * @return the authenticated user's principal
     */
    public String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName();
    }
}