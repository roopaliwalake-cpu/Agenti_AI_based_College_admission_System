package com.bcu.admission.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfirmationResponse(
        Long admissionId,
        Long paymentId,
        String studentName,
        String email,
        String courseName,
        String collegeName,
        LocalDateTime admissionDate,
        BigDecimal paidAmount,
        String paymentStatus,
        String admissionStatus
) {
}
