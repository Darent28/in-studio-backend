package com.is.in_studio.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.AdminSessionScheduleDto;
import com.is.in_studio.domain.dto.DashboardDto;
import com.is.in_studio.domain.dto.DashboardDto.MemberCreditsDto;
import com.is.in_studio.domain.dto.DashboardDto.RecentMemberDto;
import com.is.in_studio.domain.dto.DashboardDto.TopAttendeeDto;
import com.is.in_studio.domain.dto.DashboardDto.TopPackageDto;
import com.is.in_studio.domain.dto.PaymentResponseDto;
import com.is.in_studio.entity.Membership;
import com.is.in_studio.entity.Payment;
import com.is.in_studio.entity.User;
import com.is.in_studio.repository.MembershipRepository;
import com.is.in_studio.repository.PaymentRepository;
import com.is.in_studio.repository.ReservationRepository;
import com.is.in_studio.repository.UserRepository;

@Service
public class DashboardService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final ClassSessionService classSessionService;

    public DashboardService(
        PaymentRepository paymentRepository,
        ReservationRepository reservationRepository,
        UserRepository userRepository,
        MembershipRepository membershipRepository,
        ClassSessionService classSessionService
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.classSessionService = classSessionService;
    }

    public DashboardDto getDashboard() {
        BigDecimal totalCard = paymentRepository.sumByStatusAndMethod(
            Payment.PaymentStatus.COMPLETED, Payment.PaymentMethod.CARD);
        BigDecimal totalCash = paymentRepository.sumByStatusAndMethod(
            Payment.PaymentStatus.COMPLETED, Payment.PaymentMethod.CASH);
        BigDecimal totalEarnings = paymentRepository.sumByStatus(Payment.PaymentStatus.COMPLETED);

        long totalMembers = userRepository.countByRole(User.UserRole.CLIENT);

        List<AdminSessionScheduleDto> todayClasses = classSessionService.getSchedule(LocalDate.now(), null);

        List<PaymentResponseDto> recentPurchases = paymentRepository
            .findRecentByStatus(Payment.PaymentStatus.COMPLETED, PageRequest.of(0, 5))
            .stream()
            .map(PaymentResponseDto::fromEntity)
            .toList();

        List<TopAttendeeDto> topAttendees = reservationRepository
            .findTopAttendees(PageRequest.of(0, 5))
            .stream()
            .map(row -> new TopAttendeeDto(
                (Long) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (Long) row[4]
            ))
            .toList();

        List<TopPackageDto> topPackages = paymentRepository
            .findTopPackagesByStatus(Payment.PaymentStatus.COMPLETED, PageRequest.of(0, 5))
            .stream()
            .map(row -> new TopPackageDto(
                (Integer) row[0],
                (String) row[1],
                (Long) row[2]
            ))
            .toList();

        List<RecentMemberDto> recentMembers = userRepository
            .findRecentByRole(User.UserRole.CLIENT, PageRequest.of(0, 5))
            .stream()
            .map(u -> new RecentMemberDto(
                u.getUserId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getCreatedAt()
            ))
            .toList();

        List<MemberCreditsDto> memberCredits = membershipRepository
            .findByStatusWithUser(Membership.MembershipStatus.ACTIVE)
            .stream()
            .map(m -> new MemberCreditsDto(
                m.getMembershipId(),
                m.getUser().getUserId(),
                m.getUser().getFirstName(),
                m.getUser().getLastName(),
                m.getUser().getEmail(),
                m.getCreditsLeft(),
                m.getCreditsTotal() != null ? m.getCreditsTotal() : 0,
                m.getStatus().name()
            ))
            .toList();

        return new DashboardDto(
            totalCard,
            totalCash,
            totalEarnings,
            totalMembers,
            todayClasses,
            recentPurchases,
            topAttendees,
            topPackages,
            recentMembers,
            memberCredits
        );
    }
}
