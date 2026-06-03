package com.is.in_studio.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.is.in_studio.entity.User;

public record AdminUserResponseDto(
    Long userId,
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate birthdate,
    String role,
    Boolean active,
    Boolean emailVerified,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

    public static AdminUserResponseDto fromEntity(User user) {
        return new AdminUserResponseDto(
            user.getUserId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhone(),
            user.getBirthdate(),
            user.getRole() != null ? user.getRole().name() : null,
            user.getActive(),
            user.getEmailVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
