package com.tiagofsps.hunting_fishing_store.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtil {

    /**
     * Checks if the authenticated user is authorized to access a resource.
     * Admins have access to all resources, regular users only their own.
     *
     * @param requestedResourceOwnerId The ID of the resource owner
     * @param userEmail The email of the resource owner to verify
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorizedToAccess(Long requestedResourceOwnerId, String userEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Check if user is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            return true;
        }

        // Check if user owns the resource
        String authenticatedUser = authentication.getName();
        return authenticatedUser.equals(userEmail);
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
