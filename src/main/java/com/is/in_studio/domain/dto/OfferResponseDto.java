package com.is.in_studio.domain.dto;

import com.is.in_studio.entity.Offer;

public record OfferResponseDto(
    Integer offerId,
    Integer planId,
    String planName,
    Integer discountPercent,
    Integer dayOfWeek,
    String startHour,
    String endHour,
    Boolean active
) {
    public static OfferResponseDto fromEntity(Offer o) {
        return new OfferResponseDto(
            o.getOfferId(),
            o.getPlan().getPlanId(),
            o.getPlan().getName(),
            o.getDiscountPercent(),
            o.getDayOfWeek(),
            o.getStartHour() != null ? o.getStartHour().toString() : null,
            o.getEndHour()   != null ? o.getEndHour().toString()   : null,
            o.getActive()
        );
    }
}
