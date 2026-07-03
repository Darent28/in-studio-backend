package com.is.in_studio.domain.dto;

import java.time.LocalDate;
import java.util.List;

import com.is.in_studio.entity.Coupon;

public record CouponResponseDto(
    Integer couponId,
    String code,
    Integer discountPercent,
    Boolean active,
    LocalDate startDate,
    LocalDate endDate,
    List<Integer> planIds,
    List<String> planNames
) {
    public static CouponResponseDto fromEntity(Coupon c) {
        return new CouponResponseDto(
            c.getCouponId(),
            c.getCode(),
            c.getDiscountPercent(),
            c.getActive(),
            c.getStartDate(),
            c.getEndDate(),
            c.getPlans().stream().map(p -> p.getPlanId()).toList(),
            c.getPlans().stream().map(p -> p.getName()).toList()
        );
    }
}
