package com.is.in_studio.domain.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;

import com.is.in_studio.entity.Reservation;

public record ReservationResponseDto(
    Long reservationId,
    Long sessionId,
    String sessionTitle,
    String startTime,
    String endTime,
    String roomName,
    String instructorFirstName,
    String instructorLastName,
    LocalDate sessionDate,
    String status,
    int capacity,
    long reservedCount,
    long onHoldCount,
    OffsetDateTime createdAt
) {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static ReservationResponseDto fromEntity(Reservation r, long reservedCount, long onHoldCount) {
        var s = r.getSession();
        return new ReservationResponseDto(
            r.getReservationId(),
            s.getSessionId(),
            s.getTitle(),
            s.getStartTime() != null ? s.getStartTime().format(TIME_FMT) : null,
            s.getEndTime()   != null ? s.getEndTime().format(TIME_FMT)   : null,
            s.getRoom().getName(),
            s.getInstructor().getUser().getFirstName(),
            s.getInstructor().getUser().getLastName(),
            r.getSessionDate(),
            r.getStatus(),
            s.getRoom().getCapacity(),
            reservedCount,
            onHoldCount,
            r.getCreatedAt()
        );
    }
}
