package com.is.in_studio.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "membership")
public class Membership implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long membershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "credits_left", nullable = false)
    private Integer creditsLeft;

    @Column(name = "credits_total", nullable = false)
    private Integer creditsTotal = 0;

    @Column(name = "status", nullable = false, columnDefinition = "membership_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Membership() {}

    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getCreditsLeft() { return creditsLeft; }
    public void setCreditsLeft(Integer creditsLeft) { this.creditsLeft = creditsLeft; }

    public Integer getCreditsTotal() { return creditsTotal; }
    public void setCreditsTotal(Integer creditsTotal) { this.creditsTotal = creditsTotal; }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public enum MembershipStatus {
        ACTIVE, EXPIRED, CANCELLED, FROZEN
    }
}
