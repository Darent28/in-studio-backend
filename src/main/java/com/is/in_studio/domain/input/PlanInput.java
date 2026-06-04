package com.is.in_studio.domain.input;

import java.math.BigDecimal;

import com.is.in_studio.entity.Plan.PlanType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlanInput {

    @NotNull(message = "Name is required")
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Credits is required")
    @Min(value = 0, message = "Credits must be 0 or more")
    private Integer credits;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be 0 or more")
    private BigDecimal price;

    @NotNull(message = "Duration days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @NotNull(message = "Type is required")
    private PlanType type;

    private Boolean active = true;

    public PlanInput() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public PlanType getType() { return type; }
    public void setType(PlanType type) { this.type = type; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
