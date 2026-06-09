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
    LocalDate startDate,
    LocalDate endDate,
    Integer creditsLeft,
    Integer creditsTotal,
    String status,
    OffsetDateTime createdAt,
    Long lastPaymentId
) {
    public static MembershipResponseDto fromEntity(Membership m) {
        return fromEntity(m, null);
    }

    public static MembershipResponseDto fromEntity(Membership m, Long lastPaymentId) {
        return new MembershipResponseDto(
            m.getMembershipId(),
            m.getUser().getUserId(),
            m.getUser().getFirstName(),
            m.getUser().getLastName(),
            m.getUser().getEmail(),
            m.getStartDate(),
            m.getEndDate(),
            m.getCreditsLeft(),
            m.getCreditsTotal() != null ? m.getCreditsTotal() : 0,
            m.getStatus().name(),
            m.getCreatedAt(),
            lastPaymentId
        );
    }
}
