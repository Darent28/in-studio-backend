package com.is.in_studio.domain.input;

import java.util.List;

import jakarta.validation.constraints.*;

public class CouponInput {

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must be uppercase letters and digits only")
    private String code;

    @NotNull @Min(1) @Max(100)
    private Integer discountPercent;

    private Boolean active = true;

    private String startDate;

    private String endDate;

    private List<Integer> planIds;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code != null ? code.toUpperCase().trim() : null; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<Integer> getPlanIds() { return planIds; }
    public void setPlanIds(List<Integer> planIds) { this.planIds = planIds; }
}
