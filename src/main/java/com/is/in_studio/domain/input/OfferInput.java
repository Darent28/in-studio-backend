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

    /** Bitmask: bit 0 = Mon … bit 6 = Sun. 0 or null = any day. */
    private Integer daysOfWeek;

    private String startDate;

    private String endDate;

    private String startHour;

    private String endHour;

    private Boolean active = true;

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public Integer getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(Integer daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStartHour() { return startHour; }
    public void setStartHour(String startHour) { this.startHour = startHour; }

    public String getEndHour() { return endHour; }
    public void setEndHour(String endHour) { this.endHour = endHour; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
