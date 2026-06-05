package com.is.in_studio.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.is.in_studio.entity.Membership;

public record MembershipResponseDto(
    Long membershipId,
    Long userId,
    String userFirstName,
    String userLastName,
    String userEmail,
    Integer planId,
    String planName,
    String planType,
    Integer planCredits,
    LocalDate startDate,
    LocalDate endDate,
    Integer creditsLeft,
    String status,
    OffsetDateTime createdAt
) {
    public static MembershipResponseDto fromEntity(Membership m) {
        return new MembershipResponseDto(
            m.getMembershipId(),
            m.getUser().getUserId(),
            m.getUser().getFirstName(),
            m.getUser().getLastName(),
            m.getUser().getEmail(),
            m.getPlan().getPlanId(),
            m.getPlan().getName(),
            m.getPlan().getType() != null ? m.getPlan().getType().name() : null,
            m.getPlan().getCredits(),
            m.getStartDate(),
            m.getEndDate(),
            m.getCreditsLeft(),
            m.getStatus().name(),
            m.getCreatedAt()
        );
    }
}
