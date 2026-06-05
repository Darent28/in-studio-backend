package com.is.in_studio.domain.dto;

public record InstructorDto(
    Integer instructorId,
    Long userId,
    String firstName,
    String lastName,
    String email,
    String specialty,
    String bio,
    Boolean active
) {}
