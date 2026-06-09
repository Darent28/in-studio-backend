package com.is.in_studio.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.PaymentResponseDto;
import com.is.in_studio.domain.input.PaymentInput;
import com.is.in_studio.entity.Membership;
import com.is.in_studio.entity.Membership.MembershipStatus;
import com.is.in_studio.entity.Payment;
import com.is.in_studio.entity.Payment.PaymentStatus;
import com.is.in_studio.entity.Plan;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.BadRequestException;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.MembershipRepository;
import com.is.in_studio.repository.PaymentRepository;
import com.is.in_studio.repository.PlanRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          MembershipRepository membershipRepository,
                          PlanRepository planRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.membershipRepository = membershipRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    public List<PaymentResponseDto> getAll() {
        return paymentRepository.findAllWithDetails().stream()
            .map(PaymentResponseDto::fromEntity)
            .toList();
    }

    public List<PaymentResponseDto> getByMembership(Long membershipId) {
        return paymentRepository.findByMembershipId(membershipId).stream()
            .map(PaymentResponseDto::fromEntity)
            .toList();
    }

    public PaymentResponseDto getById(Long id) {
        return PaymentResponseDto.fromEntity(
            paymentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id))
        );
    }

    @Transactional
    public PaymentResponseDto create(PaymentInput input) {
        // Dedup: electronic payments may arrive from both the client confirm call and the webhook
        if (input.getTransactionRef() != null) {
            var existing = paymentRepository.findByTransactionRef(input.getTransactionRef());
            if (existing.isPresent()) {
                return PaymentResponseDto.fromEntity(existing.get());
            }
        }

        User user = userRepository.findById(input.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + input.getUserId()));
        Plan plan = planRepository.findById(input.getPlanId())
            .orElseThrow(() -> new NotFoundException("Plan not found with id: " + input.getPlanId()));

        boolean needsConfirmation = input.getMethod() == Payment.PaymentMethod.CASH
            || input.getMethod() == Payment.PaymentMethod.TRANSFER;

        int credits = plan.getCredits() != null ? plan.getCredits() : 0;

        LocalDate today = LocalDate.now();
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setStartDate(today);
        membership.setEndDate(today.plusDays(plan.getDurationDays()));
        membership.setStatus(needsConfirmation ? MembershipStatus.FROZEN : MembershipStatus.ACTIVE);
        membership.setCreditsLeft(needsConfirmation ? 0 : credits);
        membership.setCreditsTotal(needsConfirmation ? 0 : credits);
        membershipRepository.save(membership);

        Payment payment = new Payment();
        payment.setMembership(membership);
        payment.setPlan(plan);
        payment.setAmount(input.getAmount());
        payment.setCurrency(input.getCurrency() != null ? input.getCurrency() : "MXN");
        payment.setMethod(input.getMethod());
        payment.setTransactionRef(input.getTransactionRef());
        payment.setStatus(needsConfirmation ? Payment.PaymentStatus.PENDING : Payment.PaymentStatus.COMPLETED);
        if (!needsConfirmation) {
            payment.setPaidAt(OffsetDateTime.now());
        }
        paymentRepository.save(payment);

        return paymentRepository.findByIdWithDetails(payment.getPaymentId())
            .map(PaymentResponseDto::fromEntity)
            .orElse(PaymentResponseDto.fromEntity(payment));
    }

    @Transactional
    public PaymentResponseDto confirm(Long id) {
        Payment payment = paymentRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Only PENDING payments can be confirmed.");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(OffsetDateTime.now());
        paymentRepository.save(payment);

        Membership membership = payment.getMembership();
        int added = payment.getPlan().getCredits();
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setCreditsLeft(membership.getCreditsLeft() + added);
        int total = membership.getCreditsTotal() != null ? membership.getCreditsTotal() : 0;
        membership.setCreditsTotal(total + added);
        membershipRepository.save(membership);

        return PaymentResponseDto.fromEntity(payment);
    }
}
