package com.is.in_studio.entity;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "class_session")
public class ClassSession implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "days_of_week", nullable = false)
    private Integer daysOfWeek = 0;

    @Column(name = "scheduled_count", nullable = false)
    private Integer scheduledCount = 0;

    @Column(name = "status", nullable = false, columnDefinition = "session_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public ClassSession() {
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Instructor getInstructor() { return instructor; }
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(Integer daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public Integer getScheduledCount() { return scheduledCount; }
    public void setScheduledCount(Integer scheduledCount) { this.scheduledCount = scheduledCount; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public enum SessionStatus {
        SCHEDULED, CANCELLED, COMPLETED
    }
}
