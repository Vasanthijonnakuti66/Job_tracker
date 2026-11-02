package com.jobtracker.dto;

import com.jobtracker.entity.User;

/**
 * Public view of a user. Password is intentionally omitted.
 */
public record UserResponse(
        Long id,
        String email,
        String fullName
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName());
    }
}
