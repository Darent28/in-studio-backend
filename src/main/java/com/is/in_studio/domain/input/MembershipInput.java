package com.is.in_studio.domain.input;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

public class MembershipInput {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "planId is required")
    private Integer planId;

    @NotNull(message = "startDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    public MembershipInput() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}
