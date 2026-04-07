package com.is.in_studio.domain.dto;

import java.time.Instant;

public record AuthResponseDto(
    String token,
    Instant expiresAt,
    UserResponseDto user
) {}
