package com.is.in_studio.domain.dto;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.is.in_studio.entity.ClassSession;

public record ClassSessionResponseDto(
    Long sessionId,
    Integer instructorId,
    String instructorFirstName,
    String instructorLastName,
    Integer roomId,
    String roomName,
    String startTime,
    String endTime,
    List<String> days,
    Integer scheduledCount,
    String title,
    String status,
    String notes,
    OffsetDateTime createdAt
) {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static ClassSessionResponseDto fromEntity(ClassSession s) {
        return new ClassSessionResponseDto(
            s.getSessionId(),
            s.getInstructor().getInstructorId(),
            s.getInstructor().getUser().getFirstName(),
            s.getInstructor().getUser().getLastName(),
            s.getRoom().getRoomId(),
            s.getRoom().getName(),
            s.getStartTime() != null ? s.getStartTime().format(TIME_FMT) : null,
            s.getEndTime()   != null ? s.getEndTime().format(TIME_FMT)   : null,
            bitmaskToDays(s.getDaysOfWeek() != null ? s.getDaysOfWeek() : 0),
            s.getScheduledCount(),
            s.getTitle(),
            s.getStatus() != null ? s.getStatus().name() : null,
            s.getNotes(),
            s.getCreatedAt()
        );
    }

    private static List<String> bitmaskToDays(int bitmask) {
        return Arrays.stream(DayOfWeek.values())
            .filter(d -> (bitmask & (1 << (d.getValue() - 1))) != 0)
            .map(DayOfWeek::name)
            .toList();
    }
}
