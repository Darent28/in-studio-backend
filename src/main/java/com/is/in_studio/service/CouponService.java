package com.is.in_studio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.CouponResponseDto;
import com.is.in_studio.domain.input.CouponInput;
import com.is.in_studio.entity.Coupon;
import com.is.in_studio.entity.CouponUsage;
import com.is.in_studio.entity.Plan;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.BadRequestException;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.CouponRepository;
import com.is.in_studio.repository.CouponUsageRepository;
import com.is.in_studio.repository.PlanRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public CouponService(CouponRepository couponRepository,
                         CouponUsageRepository couponUsageRepository,
                         PlanRepository planRepository,
                         UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    public List<CouponResponseDto> getAll() {
        return couponRepository.findAll().stream()
            .map(CouponResponseDto::fromEntity)
            .toList();
    }

    @Transactional
    public CouponResponseDto create(CouponInput input) {
        String code = input.getCode().toUpperCase().trim();
        if (couponRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new BadRequestException("Coupon code already exists: " + code);
        }
        Coupon coupon = new Coupon();
        applyInput(coupon, input);
        return CouponResponseDto.fromEntity(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponseDto update(Integer id, CouponInput input) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Coupon not found: " + id));
        String code = input.getCode().toUpperCase().trim();
        if (couponRepository.existsByCodeIgnoreCaseAndCouponIdNot(code, id)) {
            throw new BadRequestException("Coupon code already exists: " + code);
        }
        applyInput(coupon, input);
        return CouponResponseDto.fromEntity(couponRepository.save(coupon));
    }

    @Transactional
    public void delete(Integer id) {
        couponRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Coupon not found: " + id));
        couponRepository.deleteById(id);
    }

    /**
     * Validates a coupon for the given plan and user.
     * Throws BadRequestException if the user has already used this coupon.
     * Returns empty if the coupon is not found, inactive, outside date range, or plan-scoped to another plan.
     */
    public Optional<CouponResponseDto> validate(String code, Integer planId, Long userId) {
        Optional<Coupon> found = couponRepository.findByCodeIgnoreCase(code)
            .filter(c -> Boolean.TRUE.equals(c.getActive()))
            .filter(c -> {
                LocalDate today = LocalDate.now();
                return (c.getStartDate() == null || !today.isBefore(c.getStartDate()))
                    && (c.getEndDate() == null || !today.isAfter(c.getEndDate()));
            })
            .filter(c -> c.getPlans().isEmpty()
                || c.getPlans().stream().anyMatch(p -> p.getPlanId().equals(planId)));

        if (found.isEmpty()) return Optional.empty();

        Coupon coupon = found.get();
        if (userId != null && couponUsageRepository.existsByCouponCouponIdAndUserUserId(coupon.getCouponId(), userId)) {
            throw new BadRequestException("You have already used this coupon.");
        }

        return Optional.of(CouponResponseDto.fromEntity(coupon));
    }

    /** Records that a user has used a coupon. Idempotent — silently ignores duplicate (unique constraint). */
    @Transactional
    public void recordUsage(Integer couponId, Long userId) {
        if (couponUsageRepository.existsByCouponCouponIdAndUserUserId(couponId, userId)) return;

        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new NotFoundException("Coupon not found: " + couponId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setUser(user);
        couponUsageRepository.save(usage);
    }

    private void applyInput(Coupon coupon, CouponInput input) {
        coupon.setCode(input.getCode().toUpperCase().trim());
        coupon.setDiscountPercent(input.getDiscountPercent());
        coupon.setActive(input.getActive() != null ? input.getActive() : true);
        coupon.setStartDate(input.getStartDate() != null && !input.getStartDate().isBlank()
            ? LocalDate.parse(input.getStartDate()) : null);
        coupon.setEndDate(input.getEndDate() != null && !input.getEndDate().isBlank()
            ? LocalDate.parse(input.getEndDate()) : null);

        List<Plan> plans = new ArrayList<>();
        if (input.getPlanIds() != null) {
            for (Integer planId : input.getPlanIds()) {
                plans.add(planRepository.findById(planId)
                    .orElseThrow(() -> new NotFoundException("Plan not found: " + planId)));
            }
        }
        coupon.setPlans(plans);
    }
}
