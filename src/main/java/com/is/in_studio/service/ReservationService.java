package com.is.in_studio.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.ReservationResponseDto;
import com.is.in_studio.entity.ClassSession;
import com.is.in_studio.entity.Membership;
import com.is.in_studio.entity.Membership.MembershipStatus;
import com.is.in_studio.entity.Reservation;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.BadRequestException;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.ClassSessionRepository;
import com.is.in_studio.repository.MembershipRepository;
import com.is.in_studio.repository.ReservationRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReservationService {

    private static final int MAX_ON_HOLD = 2;

    private final ReservationRepository reservationRepository;
    private final ClassSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public ReservationService(ReservationRepository reservationRepository,
                               ClassSessionRepository sessionRepository,
                               UserRepository userRepository,
                               MembershipRepository membershipRepository) {
        this.reservationRepository = reservationRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public ReservationResponseDto book(Long userId, Long sessionId, LocalDate sessionDate) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ClassSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

        // No duplicate active bookings
        reservationRepository.findActiveByUserAndSessionAndDate(userId, sessionId, sessionDate)
            .ifPresent(r -> { throw new BadRequestException("Already booked for this session on this date"); });

        long reservedCount = reservationRepository
            .countBySession_SessionIdAndSessionDateAndStatus(sessionId, sessionDate, "RESERVED");
        long onHoldCount = reservationRepository
            .countBySession_SessionIdAndSessionDateAndStatus(sessionId, sessionDate, "ON_HOLD");

        int capacity = session.getRoom().getCapacity();

        // Reuse a previously cancelled row to respect the unique constraint
        Reservation reservation = reservationRepository
            .findAnyByUserAndSessionAndDate(userId, sessionId, sessionDate)
            .orElseGet(() -> {
                Reservation r = new Reservation();
                r.setUser(user);
                r.setSession(session);
                r.setSessionDate(sessionDate);
                return r;
            });
        reservation.setMembership(null);

        if (reservedCount < capacity) {
            Membership membership = membershipRepository.findByUser_UserId(userId).stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE && m.getCreditsLeft() > 0)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No credits available to book this class"));
            membership.setCreditsLeft(membership.getCreditsLeft() - 1);
            membershipRepository.save(membership);
            reservation.setMembership(membership);
            reservation.setStatus("RESERVED");
            reservedCount++;
        } else if (onHoldCount < MAX_ON_HOLD) {
            reservation.setStatus("ON_HOLD");
            onHoldCount++;
        } else {
            throw new BadRequestException("Session is full and waitlist is full");
        }

        reservationRepository.save(reservation);
        return ReservationResponseDto.fromEntity(reservation, reservedCount, onHoldCount);
    }

    @Transactional
    public void cancel(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        if (!reservation.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Not authorized to cancel this reservation");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BadRequestException("Reservation is already cancelled");
        }

        String prevStatus = reservation.getStatus();
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);

        if ("RESERVED".equals(prevStatus)) {
            // Refund credit to the cancelling user
            if (reservation.getMembership() != null) {
                Membership m = reservation.getMembership();
                m.setCreditsLeft(m.getCreditsLeft() + 1);
                membershipRepository.save(m);
            }

            // Promote first on-hold user
            reservationRepository
                .findBySession_SessionIdAndSessionDateAndStatusOrderByCreatedAtAsc(
                    reservation.getSession().getSessionId(), reservation.getSessionDate(), "ON_HOLD")
                .stream()
                .findFirst()
                .ifPresent(held -> {
                    // Find their active membership with credits
                    Membership heldMembership = membershipRepository
                        .findByUser_UserId(held.getUser().getUserId()).stream()
                        .filter(m -> m.getStatus() == MembershipStatus.ACTIVE && m.getCreditsLeft() > 0)
                        .findFirst()
                        .orElse(null);

                    if (heldMembership != null) {
                        heldMembership.setCreditsLeft(heldMembership.getCreditsLeft() - 1);
                        membershipRepository.save(heldMembership);
                        held.setMembership(heldMembership);
                    }
                    held.setStatus("RESERVED");
                    reservationRepository.save(held);
                });
        }
    }

    public List<ReservationResponseDto> getMyReservations(Long userId) {
        return reservationRepository.findActiveByUser(userId).stream()
            .map(r -> {
                long reserved = reservationRepository.countBySession_SessionIdAndSessionDateAndStatus(
                    r.getSession().getSessionId(), r.getSessionDate(), "RESERVED");
                long onHold = reservationRepository.countBySession_SessionIdAndSessionDateAndStatus(
                    r.getSession().getSessionId(), r.getSessionDate(), "ON_HOLD");
                return ReservationResponseDto.fromEntity(r, reserved, onHold);
            })
            .toList();
    }
}
