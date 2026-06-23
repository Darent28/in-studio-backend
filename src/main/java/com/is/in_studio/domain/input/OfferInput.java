package com.is.in_studio.domain.input;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OfferInput {

    @NotNull
    private Integer planId;

    @NotNull
    @Min(1) @Max(100)
    private Integer discountPercent;

    @Min(1) @Max(7)
    private Integer dayOfWeek;

    private String startHour;

    private String endHour;

    private Boolean active = true;

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartHour() { return startHour; }
    public void setStartHour(String startHour) { this.startHour = startHour; }

    public String getEndHour() { return endHour; }
    public void setEndHour(String endHour) { this.endHour = endHour; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
