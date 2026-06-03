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
    Integer disciplineId,
    String disciplineName,
    Boolean active
) {

    public static PlanResponseDto fromEntity(Plan plan) {
        return new PlanResponseDto(
            plan.getPlanId(),
            plan.getName(),
            plan.getCredits(),
            plan.getPrice(),
            plan.getDurationDays(),
            plan.getType() != null ? plan.getType().name() : null,
            plan.getDiscipline() != null ? plan.getDiscipline().getDisciplineId() : null,
            plan.getDiscipline() != null ? plan.getDiscipline().getName() : null,
            plan.getActive()
        );
    }
}
