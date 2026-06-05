package com.is.in_studio.domain.input;

import java.util.List;

import com.is.in_studio.entity.ClassSession.SessionStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ClassSessionInput {

    @NotNull(message = "Instructor is required")
    @Min(value = 1, message = "Please select an instructor")
    private Integer instructorId;

    @NotNull(message = "Room is required")
    @Min(value = 1, message = "Please select a room")
    private Integer roomId;

    @NotNull(message = "Start time is required")
    private String startTime;

    @NotNull(message = "End time is required")
    private String endTime;

    private List<String> days = new java.util.ArrayList<>();

    private SessionStatus status = SessionStatus.SCHEDULED;

    private String notes;

    public ClassSessionInput() {
    }

    public Integer getInstructorId() { return instructorId; }
    public void setInstructorId(Integer instructorId) { this.instructorId = instructorId; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
