package com.is.in_studio.domain.dto;

import java.util.List;

public record AdminSessionScheduleDto(
    Long sessionId,
    String title,
    Integer instructorId,
    String instructorFirstName,
    String instructorLastName,
    String roomName,
    String startTime,
    String endTime,
    int capacity,
    long reservedCount,
    long onHoldCount,
    List<AttendeeDto> attendees
) {
    public record AttendeeDto(
        Long reservationId,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String status,
        String spotNumber
    ) {}
}
