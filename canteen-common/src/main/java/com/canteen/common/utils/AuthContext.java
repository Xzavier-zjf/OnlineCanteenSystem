package com.canteen.common.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Parsed authentication information from a JWT bearer token.
 */
public class AuthContext {

    private final Long userId;
    private final String username;
    private final String role;

    private AuthContext(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public static AuthContext from(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !JwtUtils.validateToken(token)) {
            throw new SecurityException("未授权访问");
        }

        return new AuthContext(
                JwtUtils.getUserIdFromToken(token),
                JwtUtils.getUsernameFromToken(token),
                JwtUtils.getRoleFromToken(token)
        );
    }

    public static String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean hasRole(String expectedRole) {
        return expectedRole != null && expectedRole.equalsIgnoreCase(role);
    }

    public boolean hasAnyRole(String... expectedRoles) {
        return Arrays.stream(expectedRoles).anyMatch(this::hasRole);
    }

    public void requireRole(String... expectedRoles) {
        if (!hasAnyRole(expectedRoles)) {
            throw new SecurityException("无权限访问");
        }
    }

    public void requireSelfOrRole(Long resourceUserId, String... allowedRoles) {
        if (resourceUserId != null && resourceUserId.equals(userId)) {
            return;
        }
        requireRole(allowedRoles);
    }
}
