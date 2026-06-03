package com.is.in_studio.domain.dto;

import com.is.in_studio.entity.User;

public record UserResponseDto(
    Long userId,
    String firstName,
    String lastName,
    String email,
    String role,
    Boolean emailVerified
) {

    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
            user.getUserId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().name() : null,
            user.getEmailVerified()
        );
    }
}
