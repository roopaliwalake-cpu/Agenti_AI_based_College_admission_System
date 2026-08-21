package com.bcu.admission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull Long admissionId,
        @Positive BigDecimal amount,
        String transactionReference
) {
}
