package com.is.in_studio.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record DashboardDto(
    BigDecimal totalEarningsCard,
    BigDecimal totalEarningsCash,
    BigDecimal totalEarnings,
    long totalMembers,
    List<AdminSessionScheduleDto> todayClasses,
    List<PaymentResponseDto> recentPurchases,
    List<TopAttendeeDto> topAttendees,
    List<TopPackageDto> topPackages,
    List<RecentMemberDto> recentMembers,
    List<MemberCreditsDto> memberCredits
) {
    public record TopAttendeeDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        long attendanceCount
    ) {}

    public record TopPackageDto(
        Integer planId,
        String planName,
        long purchaseCount
    ) {}

    public record RecentMemberDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        OffsetDateTime createdAt
    ) {}

    public record MemberCreditsDto(
        Long membershipId,
        Long userId,
        String firstName,
        String lastName,
        String email,
        Integer creditsLeft,
        Integer creditsTotal,
        String status
    ) {}
}
