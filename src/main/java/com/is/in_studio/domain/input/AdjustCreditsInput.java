package com.is.in_studio.domain.input;

import jakarta.validation.constraints.NotNull;

public class AdjustCreditsInput {

    @NotNull(message = "delta is required")
    private Integer delta;

    public AdjustCreditsInput() {}

    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }
}
