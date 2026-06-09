package com.is.in_studio.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.is.in_studio.entity.Payment;

public record PaymentResponseDto(
    Long paymentId,
    Long membershipId,
    Long userId,
    String userFirstName,
    String userLastName,
    Integer planId,
    String planName,
    Integer planCredits,
    BigDecimal amount,
    String currency,
    String method,
    String status,
    String transactionRef,
    OffsetDateTime paidAt,
    OffsetDateTime createdAt
) {
    public static PaymentResponseDto fromEntity(Payment p) {
        return new PaymentResponseDto(
            p.getPaymentId(),
            p.getMembership().getMembershipId(),
            p.getMembership().getUser().getUserId(),
            p.getMembership().getUser().getFirstName(),
            p.getMembership().getUser().getLastName(),
            p.getPlan().getPlanId(),
            p.getPlan().getName(),
            p.getPlan().getCredits(),
            p.getAmount(),
            p.getCurrency(),
            p.getMethod().name(),
            p.getStatus().name(),
            p.getTransactionRef(),
            p.getPaidAt(),
            p.getCreatedAt()
        );
    }
}
