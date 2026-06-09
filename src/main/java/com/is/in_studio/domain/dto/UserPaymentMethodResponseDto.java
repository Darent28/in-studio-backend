package com.is.in_studio.domain.dto;

import java.time.OffsetDateTime;

import com.is.in_studio.entity.UserPaymentMethod;

public record UserPaymentMethodResponseDto(
    Long id,
    String brand,
    String last4,
    Integer expMonth,
    Integer expYear,
    Boolean isDefault,
    OffsetDateTime createdAt
) {
    public static UserPaymentMethodResponseDto fromEntity(UserPaymentMethod m) {
        return new UserPaymentMethodResponseDto(
            m.getId(),
            m.getBrand(),
            m.getLast4(),
            m.getExpMonth(),
            m.getExpYear(),
            m.getIsDefault(),
            m.getCreatedAt()
        );
    }
}
