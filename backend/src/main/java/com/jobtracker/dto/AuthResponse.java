package com.jobtracker.dto;

/**
 * Returned after register/login.
 * The client stores accessToken and sends it as: Authorization: Bearer <token>
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
    public static AuthResponse of(String token, long expiresInMs, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresInMs, user);
    }
}
