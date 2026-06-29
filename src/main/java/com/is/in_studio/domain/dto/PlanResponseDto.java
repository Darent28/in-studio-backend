package com.is.in_studio.domain.dto;

import java.math.BigDecimal;

import com.is.in_studio.entity.Plan;

public record PlanResponseDto(
    Integer planId,
    String name,
    Integer credits,
    BigDecimal price,
    Integer durationDays,
    String type,
    Boolean active,
    Integer discountPercent
) {

    public static PlanResponseDto fromEntity(Plan plan, Integer discountPercent) {
        return new PlanResponseDto(
            plan.getPlanId(),
            plan.getName(),
            plan.getCredits(),
            plan.getPrice(),
            plan.getDurationDays(),
            plan.getType() != null ? plan.getType().name() : null,
            plan.getActive(),
            discountPercent
        );
    }

    public static PlanResponseDto fromEntity(Plan plan) {
        return fromEntity(plan, null);
    }
}
