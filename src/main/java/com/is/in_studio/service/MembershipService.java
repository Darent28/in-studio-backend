package com.is.in_studio.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.MembershipResponseDto;
import com.is.in_studio.domain.input.AdjustCreditsInput;
import com.is.in_studio.domain.input.ChangePeriodInput;
import com.is.in_studio.domain.input.MembershipInput;
import com.is.in_studio.entity.Membership;
import com.is.in_studio.entity.Membership.MembershipStatus;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.MembershipRepository;
import com.is.in_studio.repository.PaymentRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public MembershipService(MembershipRepository membershipRepository,
                             UserRepository userRepository,
                             PaymentRepository paymentRepository) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<MembershipResponseDto> getAll() {
        Map<Long, Long> lastPaymentByMembership;
        try {
            lastPaymentByMembership = paymentRepository.findLastPaymentIdPerMembership()
                .stream()
                .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (Long) row[1]
                ));
        } catch (Exception e) {
            lastPaymentByMembership = Map.of();
        }
        final Map<Long, Long> lpbm = lastPaymentByMembership;
        return membershipRepository.findAll().stream()
            .map(m -> MembershipResponseDto.fromEntity(m, lpbm.get(m.getMembershipId())))
            .toList();
    }

    public List<MembershipResponseDto> getByUser(Long userId) {
        return membershipRepository.findByUser_UserId(userId).stream()
            .map(MembershipResponseDto::fromEntity)
            .toList();
    }

    @Transactional
    public MembershipResponseDto create(MembershipInput input) {
        User user = userRepository.findById(input.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + input.getUserId()));

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setStartDate(input.getStartDate());
        membership.setEndDate(input.getEndDate());
        membership.setCreditsLeft(0);
        membership.setStatus(MembershipStatus.ACTIVE);
        membershipRepository.save(membership);
        return MembershipResponseDto.fromEntity(membership);
    }

    @Transactional
    public MembershipResponseDto adjustCredits(Long id, AdjustCreditsInput input) {
        Membership membership = membershipRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Membership not found with id: " + id));
        int updated = Math.max(0, membership.getCreditsLeft() + input.getDelta());
        membership.setCreditsLeft(updated);
        if (input.getDelta() > 0) {
            int total = membership.getCreditsTotal() != null ? membership.getCreditsTotal() : 0;
            membership.setCreditsTotal(total + input.getDelta());
        }
        membershipRepository.save(membership);
        return MembershipResponseDto.fromEntity(membership);
    }

    @Transactional
    public MembershipResponseDto changeStatus(Long id, MembershipStatus status) {
        Membership membership = membershipRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Membership not found with id: " + id));
        membership.setStatus(status);
        membershipRepository.save(membership);
        return MembershipResponseDto.fromEntity(membership);
    }

    @Transactional
    public MembershipResponseDto changePeriod(Long id, ChangePeriodInput input) {
        Membership membership = membershipRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Membership not found with id: " + id));
        membership.setStartDate(input.getStartDate());
        membership.setEndDate(input.getEndDate());
        membershipRepository.save(membership);
        return MembershipResponseDto.fromEntity(membership);
    }

    @Transactional
    public void delete(Long id) {
        membershipRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Membership not found with id: " + id));
        membershipRepository.deleteById(id);
    }
}
