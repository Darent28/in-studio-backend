package com.is.in_studio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.MembershipResponseDto;
import com.is.in_studio.domain.input.AdjustCreditsInput;
import com.is.in_studio.domain.input.MembershipInput;
import com.is.in_studio.entity.Membership;
import com.is.in_studio.entity.Membership.MembershipStatus;
import com.is.in_studio.entity.Plan;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.MembershipRepository;
import com.is.in_studio.repository.PlanRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    public MembershipService(MembershipRepository membershipRepository,
                             UserRepository userRepository,
                             PlanRepository planRepository) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    public List<MembershipResponseDto> getAll() {
        return membershipRepository.findAll().stream()
            .map(MembershipResponseDto::fromEntity)
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
        Plan plan = planRepository.findById(input.getPlanId())
            .orElseThrow(() -> new NotFoundException("Plan not found with id: " + input.getPlanId()));

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setPlan(plan);
        membership.setStartDate(input.getStartDate());
        membership.setEndDate(input.getStartDate().plusDays(plan.getDurationDays()));
        membership.setCreditsLeft(plan.getCredits());
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
    public void delete(Long id) {
        membershipRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Membership not found with id: " + id));
        membershipRepository.deleteById(id);
    }
}
