package com.is.in_studio.domain.dto;

import java.time.OffsetDateTime;

import com.is.in_studio.entity.ClassSession;

public record ClassSessionResponseDto(
    Long sessionId,
    Integer disciplineId,
    String disciplineName,
    Integer instructorId,
    String instructorFirstName,
    String instructorLastName,
    Integer roomId,
    String roomName,
    OffsetDateTime startDatetime,
    OffsetDateTime endDatetime,
    Integer capacity,
    Integer bookedCount,
    String status,
    String notes,
    OffsetDateTime createdAt
) {

    public static ClassSessionResponseDto fromEntity(ClassSession s) {
        return new ClassSessionResponseDto(
            s.getSessionId(),
            s.getDiscipline().getDisciplineId(),
            s.getDiscipline().getName(),
            s.getInstructor().getInstructorId(),
            s.getInstructor().getFirstName(),
            s.getInstructor().getLastName(),
            s.getRoom().getRoomId(),
            s.getRoom().getName(),
            s.getStartDatetime(),
            s.getEndDatetime(),
            s.getCapacity(),
            s.getBookedCount(),
            s.getStatus() != null ? s.getStatus().name() : null,
            s.getNotes(),
            s.getCreatedAt()
        );
    }
}
