package com.is.in_studio.domain.input;

import java.time.OffsetDateTime;

import com.is.in_studio.entity.ClassSession.SessionStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ClassSessionInput {

    @NotNull(message = "Discipline is required")
    private Integer disciplineId;

    @NotNull(message = "Instructor is required")
    private Integer instructorId;

    @NotNull(message = "Room is required")
    private Integer roomId;

    @NotNull(message = "Start datetime is required")
    private OffsetDateTime startDatetime;

    @NotNull(message = "End datetime is required")
    private OffsetDateTime endDatetime;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private SessionStatus status = SessionStatus.SCHEDULED;

    private String notes;

    public ClassSessionInput() {
    }

    public Integer getDisciplineId() { return disciplineId; }
    public void setDisciplineId(Integer disciplineId) { this.disciplineId = disciplineId; }

    public Integer getInstructorId() { return instructorId; }
    public void setInstructorId(Integer instructorId) { this.instructorId = instructorId; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public OffsetDateTime getStartDatetime() { return startDatetime; }
    public void setStartDatetime(OffsetDateTime startDatetime) { this.startDatetime = startDatetime; }

    public OffsetDateTime getEndDatetime() { return endDatetime; }
    public void setEndDatetime(OffsetDateTime endDatetime) { this.endDatetime = endDatetime; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
